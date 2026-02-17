package com.myvet.dataaccess.enums;

public class Enums {

    public enum HealthStatus {
        healthy, needs_attention, overdue_vaccine
    }

    public enum DrugType {
        antibiotic, pain_killer, internal_parasite, external_parasite,
        vaccine, supplement, hormonal, other
    }

    public enum MedicalContext {
        routine, treatment, chronic
    }

    public enum OperationType {
        surgery, orthopedic, soft_tissue, emergency, diagnostic
    }

    public enum ReportType {
        lab_result, anamnesis, imaging, examination, prescription, other
    }

    public enum LogType {
        walk, sleep, feed, urinate
    }

    public enum Zaman {
        gun, hafta, ay
    }

    public enum PaymentType {
        gelir, gider
    }

    public enum ProductCategory {
        ilac, mama, asi, pet_malzemeleri, genel
    }

    public enum AppointmentStatus {
        scheduled, completed, cancelled, no_show
    }
}
