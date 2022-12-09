package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import java.time.LocalDate;
import java.util.Objects;

public class SickNote {

    private final Integer id;
    private final Person person;
    private final Person applier;
    private final SickNoteType sickNoteType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final DayLength dayLength;
    private final LocalDate aubStartDate;
    private final LocalDate aubEndDate;
    private final LocalDate lastEdited;
    private final LocalDate endOfSickPayNotificationSend;
    private final SickNoteStatus status;

    private SickNote(Integer id, Person person, Person applier, SickNoteType sickNoteType, LocalDate startDate,
                     LocalDate endDate, DayLength dayLength, LocalDate aubStartDate, LocalDate aubEndDate,
                     LocalDate lastEdited, LocalDate endOfSickPayNotificationSend, SickNoteStatus status) {

        this.id = id;
        this.person = person;
        this.applier = applier;
        this.sickNoteType = sickNoteType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dayLength = dayLength;
        this.aubStartDate = aubStartDate;
        this.aubEndDate = aubEndDate;
        this.lastEdited = lastEdited;
        this.endOfSickPayNotificationSend = endOfSickPayNotificationSend;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public Person getPerson() {
        return person;
    }

    public Person getApplier() {
        return applier;
    }

    public SickNoteType getSickNoteType() {
        return sickNoteType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public LocalDate getAubStartDate() {
        return aubStartDate;
    }

    public LocalDate getAubEndDate() {
        return aubEndDate;
    }

    public LocalDate getLastEdited() {
        return lastEdited;
    }

    public LocalDate getEndOfSickPayNotificationSend() {
        return endOfSickPayNotificationSend;
    }

    public SickNoteStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return SickNoteStatus.ACTIVE.equals(getStatus());
    }

    public boolean isAubPresent() {
        return getAubStartDate() != null && getAubEndDate() != null;
    }

    public Period getPeriod() {
        return new Period(getStartDate(), getEndDate(), getDayLength());
    }

    @Override
    public String toString() {
        return "SickNote{" +
                "id=" + id +
                ", person=" + person +
                ", applier=" + applier +
                ", sickNoteType=" + sickNoteType +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", dayLength=" + dayLength +
                ", aubStartDate=" + aubStartDate +
                ", aubEndDate=" + aubEndDate +
                ", lastEdited=" + lastEdited +
                ", endOfSickPayNotificationSend=" + endOfSickPayNotificationSend +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SickNote sickNote = (SickNote) o;
        return Objects.equals(person, sickNote.person)
                && Objects.equals(applier, sickNote.applier)
                && Objects.equals(sickNoteType, sickNote.sickNoteType)
                && Objects.equals(startDate, sickNote.startDate)
                && Objects.equals(endDate, sickNote.endDate)
                && dayLength == sickNote.dayLength;
    }

    @Override
    public int hashCode() {
        return Objects.hash(person, applier, sickNoteType, startDate, endDate, dayLength);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(SickNote sickNote) {
        return new Builder()
                .id(sickNote.getId())
                .person(sickNote.getPerson())
                .applier(sickNote.getApplier())
                .sickNoteType(sickNote.getSickNoteType())
                .startDate(sickNote.getStartDate())
                .endDate(sickNote.getEndDate())
                .dayLength(sickNote.getDayLength())
                .aubStartDate(sickNote.getAubStartDate())
                .aubEndDate(sickNote.getAubEndDate())
                .lastEdited(sickNote.getLastEdited())
                .endOfSickPayNotificationSend(sickNote.getEndOfSickPayNotificationSend())
                .status(sickNote.getStatus());
    }

    public static class Builder {
        private Integer id;
        private Person person;
        private Person applier;
        private SickNoteType sickNoteType;
        private LocalDate startDate;
        private LocalDate endDate;
        private DayLength dayLength;
        private LocalDate aubStartDate;
        private LocalDate aubEndDate;
        private LocalDate lastEdited;
        private LocalDate endOfSickPayNotificationSend;
        private SickNoteStatus status;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder person(Person person) {
            this.person = person;
            return this;
        }

        public Builder applier(Person applier) {
            this.applier = applier;
            return this;
        }

        public Builder sickNoteType(SickNoteType sickNoteType) {
            this.sickNoteType = sickNoteType;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder dayLength(DayLength dayLength) {
            this.dayLength = dayLength;
            return this;
        }

        public Builder aubStartDate(LocalDate aubStartDate) {
            this.aubStartDate = aubStartDate;
            return this;
        }

        public Builder aubEndDate(LocalDate aubEndDate) {
            this.aubEndDate = aubEndDate;
            return this;
        }

        public Builder lastEdited(LocalDate lastEdited) {
            this.lastEdited = lastEdited;
            return this;
        }

        public Builder endOfSickPayNotificationSend(LocalDate endOfSickPayNotificationSend) {
            this.endOfSickPayNotificationSend = endOfSickPayNotificationSend;
            return this;
        }

        public Builder status(SickNoteStatus status) {
            this.status = status;
            return this;
        }

        public SickNote build() {
            return new SickNote(id, person, applier, sickNoteType, startDate, endDate, dayLength, aubStartDate,
                    aubEndDate, lastEdited, endOfSickPayNotificationSend, status);
        }
    }
}
