package org.synyx.urlaubsverwaltung.settings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.account.AccountProperties;
import org.synyx.urlaubsverwaltung.calendarintegration.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeProperties;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettingsService;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.List;
import java.util.TimeZone;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings")
public class SettingsViewController {

    private final AccountProperties accountProperties;
    private final WorkingTimeProperties workingTimeProperties;
    private final SettingsService settingsService;
    private final List<CalendarProvider> calendarProviders;
    private final SettingsValidator settingsValidator;
    private final WorkingTimeSettingsService workingTimeSettingsService;
    private final Clock clock;

    @Autowired
    public SettingsViewController(AccountProperties accountProperties, WorkingTimeProperties workingTimeProperties,
                                  SettingsService settingsService, List<CalendarProvider> calendarProviders,
                                  SettingsValidator settingsValidator, WorkingTimeSettingsService workingTimeSettingsService, Clock clock) {
        this.accountProperties = accountProperties;
        this.workingTimeProperties = workingTimeProperties;
        this.settingsService = settingsService;
        this.calendarProviders = calendarProviders;
        this.settingsValidator = settingsValidator;
        this.workingTimeSettingsService = workingTimeSettingsService;
        this.clock = clock;
    }

    @GetMapping("/old")
    @PreAuthorize(IS_OFFICE)
    public String settingsDetailsOld(@RequestParam(value = "oautherrors", required = false) String googleOAuthError,
                                  HttpServletRequest request, Model model) {

        final String requestURL = request.getRequestURL().toString();
        final String authorizedRedirectUrl = getAuthorizedRedirectUrl(requestURL, "/google-api-handshake");

        final Settings settings = settingsService.getSettings();
        fillModel(model, settings, authorizedRedirectUrl);

        if (shouldShowOAuthError(googleOAuthError, settings)) {
            model.addAttribute("errors", googleOAuthError);
            model.addAttribute("oautherrors", googleOAuthError);
        }

        return "settings/settings_form";
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(@RequestParam(value = "oautherrors", required = false) String googleOAuthError,
                                  HttpServletRequest request, Model model) {

        final String requestURL = request.getRequestURL().toString();
        final String authorizedRedirectUrl = getAuthorizedRedirectUrl(requestURL, "/google-api-handshake");

        final Settings settings = settingsService.getSettings();
        fillModel(model, settings, authorizedRedirectUrl);

        if (shouldShowOAuthError(googleOAuthError, settings)) {
            model.addAttribute("errors", googleOAuthError);
            model.addAttribute("oautherrors", googleOAuthError);
        }

        model.addAttribute("workingtimesettings", workingTimeSettingsService.getSettingsDto());

        return "settings/settings_overview";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(@ModelAttribute("settings") Settings settings,
                                @RequestParam(value = "googleOAuthButton", required = false) String googleOAuthButton,
                                Errors errors, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        settingsValidator.validate(settings, errors);
        if (errors.hasErrors()) {

            final StringBuffer requestURL = request.getRequestURL();
            final String authorizedRedirectUrl = getAuthorizedRedirectUrl(requestURL.toString(), "oautherrors");

            fillModel(model, settings, authorizedRedirectUrl);

            model.addAttribute("errors", errors);

            return "settings/settings_form";
        }

        settingsService.save(processGoogleRefreshToken(settings));

        if (googleOAuthButton != null) {
            return "redirect:/web/google-api-handshake";
        }

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings";
    }

    String getAuthorizedRedirectUrl(String requestURL, String redirectPath) {
        return requestURL.replace("/settings", redirectPath);
    }

    private void fillModel(Model model, Settings settings, String authorizedRedirectUrl) {
        model.addAttribute("defaultVacationDaysFromSettings", accountProperties.getDefaultVacationDays() == -1);
        model.addAttribute("defaultWorkingTimeFromSettings", workingTimeProperties.isDefaultWorkingDaysDeactivated());

        model.addAttribute("settings", settings);
        model.addAttribute("federalStateTypes", FederalState.values());
        model.addAttribute("dayLengthTypes", DayLength.values());

        final List<String> providers = calendarProviders.stream()
            .map(provider -> provider.getClass().getSimpleName())
            .sorted(reverseOrder())
            .collect(toList());
        model.addAttribute("providers", providers);

        model.addAttribute("availableTimezones", List.of(TimeZone.getAvailableIDs()));

        if (settings.getCalendarSettings().getExchangeCalendarSettings().getTimeZoneId() == null) {
            settings.getCalendarSettings().getExchangeCalendarSettings().setTimeZoneId(clock.getZone().getId());
        }

        model.addAttribute("authorizedRedirectUrl", authorizedRedirectUrl);
    }

    private Settings processGoogleRefreshToken(Settings settingsUpdate) {
        final Settings storedSettings = settingsService.getSettings();

        final GoogleCalendarSettings storedGoogleSettings = storedSettings.getCalendarSettings().getGoogleCalendarSettings();
        final GoogleCalendarSettings updateGoogleSettings = settingsUpdate.getCalendarSettings().getGoogleCalendarSettings();

        updateGoogleSettings.setRefreshToken(storedGoogleSettings.getRefreshToken());

        if (refreshTokenGotInvalid(storedGoogleSettings, updateGoogleSettings)) {
            // refresh token is invalid if settings changed
            updateGoogleSettings.setRefreshToken(null);
        }

        return settingsUpdate;
    }

    private boolean refreshTokenGotInvalid(GoogleCalendarSettings oldSettings, GoogleCalendarSettings newSettings) {
        if (oldSettings.getClientSecret() == null
            || oldSettings.getClientId() == null
            || oldSettings.getCalendarId() == null) {
            return true;
        }

        return !oldSettings.equals(newSettings);
    }

    private boolean shouldShowOAuthError(String googleOAuthError, Settings settings) {
        return googleOAuthError != null
            && !googleOAuthError.isEmpty()
            && settings.getCalendarSettings().getGoogleCalendarSettings().getRefreshToken() == null;
    }
}
