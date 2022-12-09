package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import com.opencsv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.List;

import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

@ExtendWith(MockitoExtension.class)
class SickDaysDetailedStatisticsCsvExportServiceTest {

    @Mock
    private MessageSource messageSource;

    private SickDaysDetailedStatisticsCsvExportService sut;

    @BeforeEach
    void setUp() {
        sut = new SickDaysDetailedStatisticsCsvExportService(messageSource, new DateFormatAware());
    }

    @Test
    void getFileNameForComplete2018() {
        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage("sicknotes.statistics", new String[]{}, GERMAN)).thenReturn("test");

        final String fileName = sut.fileName(period);
        assertThat(fileName).isEqualTo("test_01012018_31122018.csv");
    }

    @Test
    void getFileNameForComplete2019() {
        final LocalDate startDate = LocalDate.parse("2019-01-01");
        final LocalDate endDate = LocalDate.parse("2019-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        when(messageSource.getMessage(eq("sicknotes.statistics"), any(), any())).thenReturn("test");

        String fileName = sut.fileName(period);
        assertThat(fileName).isEqualTo("test_01012019_31122019.csv");
    }

    @Test
    void writeStatisticsForOnePerson() {
        final LocalDate startDate = LocalDate.parse("2022-01-01");
        final LocalDate endDate = LocalDate.parse("2022-12-31");
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        final Person person = new Person();
        person.setFirstName("personOneFirstName");
        person.setLastName("personOneLastName");

        final SickNoteType sickNoteTypeSick = new SickNoteType();
        sickNoteTypeSick.setCategory(SICK_NOTE);
        sickNoteTypeSick.setMessageKey("application.data.sicknotetype.sicknote");

        final SickNoteType sickNoteTypeSickChild = new SickNoteType();
        sickNoteTypeSickChild.setCategory(SICK_NOTE_CHILD);
        sickNoteTypeSickChild.setMessageKey("application.data.sicknotetype.sicknotechild");

        final SickNote sickNote = SickNote.builder()
                .dayLength(FULL)
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .sickNoteType(sickNoteTypeSick)
                .build();

        final SickNote sickNoteHalfDayMorning = SickNote.builder()
                .dayLength(MORNING)
                .startDate(startDate)
                .endDate(startDate)
                .sickNoteType(sickNoteTypeSick)
                .build();

        final SickNote sickNoteHalfDayNoon = SickNote.builder()
                .dayLength(NOON)
                .startDate(startDate)
                .endDate(startDate)
                .sickNoteType(sickNoteTypeSick)
                .build();

        final SickNote sickNoteWithAub = SickNote.builder()
                .dayLength(FULL)
                .startDate(startDate.plusDays(3))
                .endDate(startDate.plusDays(4))
                .aubStartDate(startDate.plusDays(3))
                .aubEndDate(startDate.plusDays(4))
                .sickNoteType(sickNoteTypeSickChild)
                .build();

        final List<SickNote> sickNotes = List.of(sickNote, sickNoteWithAub, sickNoteHalfDayMorning, sickNoteHalfDayNoon);
        final List<String> departments = List.of("Here", "There");
        final SickDaysDetailedStatistics sickDaysDetailedStatistics = new
            SickDaysDetailedStatistics("42", person, sickNotes, departments);

        final List<SickDaysDetailedStatistics> statistics = List.of(sickDaysDetailedStatistics);

        addMessageSource("absence.period");
        addMessageSource("person.account.basedata.personnelNumber");
        addMessageSource("person.data.firstName");
        addMessageSource("person.data.lastName");
        addMessageSource("sicknotes.statistics.departments");
        addMessageSource("sicknotes.statistics.from");
        addMessageSource("sicknotes.statistics.to");
        addMessageSource("sicknotes.statistics.length");
        addMessageSource("sicknotes.statistics.type");
        addMessageSource("sicknotes.statistics.certificate.from");
        addMessageSource("sicknotes.statistics.certificate.to");
        addMessageSource("FULL");
        addMessageSource("MORNING");
        addMessageSource("NOON");
        addMessageSource("application.data.sicknotetype.sicknote");
        addMessageSource("application.data.sicknotetype.sicknotechild");

        final CSVWriter csvWriter = mock(CSVWriter.class);
        sut.write(period, statistics, csvWriter);

        verify(csvWriter).writeNext(new String[]{"{absence.period}: 01.01.2022 - 31.12.2022"});
        verify(csvWriter).writeNext(new String[]{"{person.account.basedata.personnelNumber}", "{person.data.firstName}", "{person.data.lastName}", "{sicknotes.statistics.departments}", "{sicknotes.statistics.from}", "{sicknotes.statistics.to}", "{sicknotes.statistics.length}", "{sicknotes.statistics.type}", "{sicknotes.statistics.certificate.from}", "{sicknotes.statistics.certificate.to}"});
        verify(csvWriter).writeNext(new String[]{"42", "personOneFirstName", "personOneLastName", "Here, There", "01.01.2022", "02.01.2022", "{FULL}", "{application.data.sicknotetype.sicknote}", null, null});
        verify(csvWriter).writeNext(new String[]{"42", "personOneFirstName", "personOneLastName", "Here, There", "04.01.2022", "05.01.2022", "{FULL}", "{application.data.sicknotetype.sicknotechild}", "04.01.2022", "05.01.2022"});
        verify(csvWriter).writeNext(new String[]{"42", "personOneFirstName", "personOneLastName", "Here, There", "01.01.2022", "01.01.2022", "{MORNING}", "{application.data.sicknotetype.sicknote}", null, null});
        verify(csvWriter).writeNext(new String[]{"42", "personOneFirstName", "personOneLastName", "Here, There", "01.01.2022", "01.01.2022", "{NOON}", "{application.data.sicknotetype.sicknote}", null, null});
    }

    private void addMessageSource(String key) {
        when(messageSource.getMessage(eq(key), any(), any())).thenReturn(String.format("{%s}", key));
    }
}
