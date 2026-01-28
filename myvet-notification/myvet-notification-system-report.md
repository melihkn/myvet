# MyVet Notification System - Implementation Report

## Table of Contents

1. [Overview](#overview)
2. [Option 1: Manual Notifications](#option-1-manual-notifications)
3. [Option 2: Appointment Event Notifications](#option-2-appointment-event-notifications)
4. [Option 3: Recurring Treatment Reminders](#option-3-recurring-treatment-reminders)
5. [Shared Components](#shared-components)
6. [Architecture Summary](#architecture-summary)
7. [Implementation Priority](#implementation-priority)

---

## Overview

The MyVet notification system consists of three distinct notification mechanisms, each serving a different use case:

| Option | Trigger | Use Case | Timing |
|--------|---------|----------|--------|
| **1. Manual** | Vet initiates | Custom messages, urgent notices | Immediate |
| **2. Appointment Events** | System events | Booking confirmations, cancellations | Immediate (async) |
| **3. Recurring Treatments** | Scheduled job | Vaccine reminders, checkup alerts | Daily batch |

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         NOTIFICATION ARCHITECTURE                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────┐     ┌─────────────┐     ┌─────────────────────┐          │
│   │   MANUAL    │     │   EVENTS    │     │     SCHEDULER       │          │
│   │             │     │             │     │                     │          │
│   │ Vet clicks  │     │ Appointment │     │ Cron job runs       │          │
│   │ "Send"      │     │ CRUD ops    │     │ every morning       │          │
│   └──────┬──────┘     └──────┬──────┘     └──────────┬──────────┘          │
│          │                   │                       │                      │
│          │                   │                       │                      │
│          ▼                   ▼                       ▼                      │
│   ┌─────────────────────────────────────────────────────────────┐          │
│   │                  NOTIFICATION SERVICE                        │          │
│   │                                                              │          │
│   │  • Resolves user preferences                                │          │
│   │  • Routes to appropriate providers                          │          │
│   │  • Logs all notifications                                   │          │
│   └─────────────────────────────────────────────────────────────┘          │
│                              │                                              │
│          ┌───────────────────┼───────────────────┐                         │
│          ▼                   ▼                   ▼                          │
│   ┌────────────┐      ┌────────────┐      ┌────────────┐                   │
│   │   EMAIL    │      │  WHATSAPP  │      │    PUSH    │                   │
│   │  Provider  │      │  Provider  │      │  Provider  │                   │
│   └────────────┘      └────────────┘      └────────────┘                   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Option 1: Manual Notifications

### Description

Veterinarian manually sends a notification to a pet owner through the system. Used for custom messages, urgent notices, or any communication not covered by automated notifications.

### Use Cases

- "Your pet's lab results are ready"
- "Please call us regarding Max's condition"
- "We have availability tomorrow if you'd like to reschedule"
- Custom reminders or follow-ups

### Technical Approach

**Pattern:** Direct API Call (Request → Service → Provider)

### Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    MANUAL NOTIFICATION FLOW                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   VET (Frontend)                                                │
│       │                                                          │
│       │ POST /api/notifications/send                            │
│       │ {                                                        │
│       │     "petOwnerId": 123,                                  │
│       │     "subject": "Lab Results Ready",                     │
│       │     "message": "Your pet's results are ready..."        │
│       │ }                                                        │
│       │                                                          │
│       ▼                                                          │
│   NotificationController                                        │
│       │                                                          │
│       │ @PostMapping("/send")                                   │
│       │ Validates request                                       │
│       │ Checks vet authorization                                │
│       │                                                          │
│       ▼                                                          │
│   NotificationService                                           │
│       │                                                          │
│       │ sendToUser(petOwnerId, subject, message)               │
│       │ Fetches user preferences                                │
│       │ Routes to enabled providers                             │
│       │                                                          │
│       ▼                                                          │
│   NotificationProvider(s)                                       │
│       │                                                          │
│       │ Email / WhatsApp / Push / SMS                          │
│       │                                                          │
│       ▼                                                          │
│   NotificationLog (saved to DB)                                 │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Components Required

| Component | Module | Description |
|-----------|--------|-------------|
| `SendNotificationRequest` | `myvet-notification` | DTO for API request |
| `NotificationController` | `myvet-notification` | REST endpoint |
| `NotificationService` | `myvet-notification` | Business logic |
| `NotificationProvider` | `myvet-notification` | Interface + implementations |
| `NotificationLog` | `myvet-data-access` | Entity for audit trail |
| `NotificationPreference` | `myvet-data-access` | User channel preferences |

### File Structure

```
myvet-notification/
├── controller/
│   └── NotificationController.java
├── dto/
│   └── SendNotificationRequest.java
├── service/
│   └── NotificationService.java
└── provider/
    ├── NotificationProvider.java (interface)
    ├── EmailProvider.java
    ├── WhatsAppProvider.java
    ├── PushNotificationProvider.java
    └── SmsProvider.java

myvet-data-access/
├── entity/
│   ├── NotificationLog.java
│   ├── NotificationPreference.java
│   ├── NotificationType.java (enum)
│   └── NotificationStatus.java (enum)
└── repository/
    ├── NotificationLogRepository.java
    └── NotificationPreferenceRepository.java
```

### Code Examples

#### DTO

```java
// myvet-notification/src/main/java/com/myvet/notification/dto/SendNotificationRequest.java
package com.myvet.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendNotificationRequest {
    
    @NotNull(message = "Pet owner ID is required")
    private Long petOwnerId;
    
    private Long petId;  // Optional: for context
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Message is required")
    private String message;
}
```

#### Controller

```java
// myvet-notification/src/main/java/com/myvet/notification/controller/NotificationController.java
package com.myvet.notification.controller;

import com.myvet.notification.dto.SendNotificationRequest;
import com.myvet.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
    public ResponseEntity<String> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        
        notificationService.sendToUser(
            request.getPetOwnerId(),
            request.getSubject(),
            request.getMessage()
        );
        
        return ResponseEntity.ok("Notification sent successfully");
    }
}
```

### Security Considerations

- Only VET and ADMIN roles can send manual notifications
- Vet can only send to pet owners of their patients
- Rate limiting to prevent spam
- All notifications logged for audit

### Pros & Cons

| Pros | Cons |
|------|------|
| Full control for vets | Requires manual action |
| Flexible messaging | Can be forgotten |
| Immediate delivery | No automation |
| Personal touch | Time-consuming for vets |

---

## Option 2: Appointment Event Notifications

### Description

System automatically sends notifications when appointment-related events occur (create, update, delete). Uses Spring's event-driven architecture for loose coupling.

### Use Cases

| Event | Notification to Pet Owner | Notification to Vet |
|-------|---------------------------|---------------------|
| Appointment Created | "Your appointment is confirmed for..." | "New appointment scheduled..." |
| Appointment Updated | "Your appointment has been rescheduled to..." | "Appointment rescheduled..." |
| Appointment Cancelled | "Your appointment has been cancelled..." | "Appointment cancelled..." |
| Appointment Reminder | "Reminder: You have an appointment tomorrow..." | (optional) |

### Technical Approach

**Pattern:** Event-Driven Architecture (Publisher → Event → Listener → Service)

### Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│               APPOINTMENT EVENT NOTIFICATION FLOW                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   AppointmentService (myvet-appointment)                        │
│       │                                                          │
│       │ createAppointment()                                     │
│       │ updateAppointment()                                     │
│       │ deleteAppointment()                                     │
│       │                                                          │
│       ▼                                                          │
│   ApplicationEventPublisher                                     │
│       │                                                          │
│       │ publishEvent(new AppointmentCreatedEvent(...))         │
│       │ publishEvent(new AppointmentUpdatedEvent(...))         │
│       │ publishEvent(new AppointmentCancelledEvent(...))       │
│       │                                                          │
│       │         ┌─────────────────────────────────┐             │
│       │         │      SPRING EVENT BUS           │             │
│       │         │                                 │             │
│       └────────►│  Decouples publisher from       │             │
│                 │  listener (loose coupling)      │             │
│                 │                                 │             │
│                 └───────────────┬─────────────────┘             │
│                                 │                                │
│                                 ▼                                │
│   AppointmentEventListener (myvet-notification)                 │
│       │                                                          │
│       │ @EventListener                                          │
│       │ @Async (non-blocking)                                   │
│       │                                                          │
│       │ onAppointmentCreated(event)                             │
│       │ onAppointmentUpdated(event)                             │
│       │ onAppointmentCancelled(event)                           │
│       │                                                          │
│       ▼                                                          │
│   NotificationService                                           │
│       │                                                          │
│       │ sendToUser(petOwnerId, ...)                            │
│       │ sendToUser(vetId, ...)                                 │
│       │                                                          │
│       ▼                                                          │
│   NotificationProvider(s) → NotificationLog                     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Components Required

| Component | Module | Description |
|-----------|--------|-------------|
| `AppointmentCreatedEvent` | `myvet-appointment` | Event class |
| `AppointmentUpdatedEvent` | `myvet-appointment` | Event class |
| `AppointmentCancelledEvent` | `myvet-appointment` | Event class |
| `AppointmentService` | `myvet-appointment` | Publishes events |
| `AppointmentEventListener` | `myvet-notification` | Listens & triggers notifications |
| `NotificationService` | `myvet-notification` | Sends notifications |

### File Structure

```
myvet-appointment/
├── event/
│   ├── AppointmentEvent.java (abstract base)
│   ├── AppointmentCreatedEvent.java
│   ├── AppointmentUpdatedEvent.java
│   └── AppointmentCancelledEvent.java
└── service/
    └── AppointmentService.java (publishes events)

myvet-notification/
└── listener/
    └── AppointmentEventListener.java (listens to events)
```

### Code Examples

#### Base Event Class

```java
// myvet-appointment/src/main/java/com/myvet/appointment/event/AppointmentEvent.java
package com.myvet.appointment.event;

import com.myvet.dataaccess.entity.Appointment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class AppointmentEvent extends ApplicationEvent {
    
    private final Appointment appointment;

    public AppointmentEvent(Object source, Appointment appointment) {
        super(source);
        this.appointment = appointment;
    }
}
```

#### Specific Event Classes

```java
// myvet-appointment/src/main/java/com/myvet/appointment/event/AppointmentCreatedEvent.java
package com.myvet.appointment.event;

import com.myvet.dataaccess.entity.Appointment;

public class AppointmentCreatedEvent extends AppointmentEvent {
    
    public AppointmentCreatedEvent(Object source, Appointment appointment) {
        super(source, appointment);
    }
}
```

```java
// myvet-appointment/src/main/java/com/myvet/appointment/event/AppointmentUpdatedEvent.java
package com.myvet.appointment.event;

import com.myvet.dataaccess.entity.Appointment;
import lombok.Getter;

@Getter
public class AppointmentUpdatedEvent extends AppointmentEvent {
    
    private final Appointment previousState;
    
    public AppointmentUpdatedEvent(Object source, Appointment appointment, Appointment previousState) {
        super(source, appointment);
        this.previousState = previousState;
    }
}
```

```java
// myvet-appointment/src/main/java/com/myvet/appointment/event/AppointmentCancelledEvent.java
package com.myvet.appointment.event;

import com.myvet.dataaccess.entity.Appointment;
import lombok.Getter;

@Getter
public class AppointmentCancelledEvent extends AppointmentEvent {
    
    private final String cancellationReason;
    
    public AppointmentCancelledEvent(Object source, Appointment appointment, String reason) {
        super(source, appointment);
        this.cancellationReason = reason;
    }
}
```

#### Event Publisher (AppointmentService)

```java
// myvet-appointment/src/main/java/com/myvet/appointment/service/AppointmentService.java
package com.myvet.appointment.service;

import com.myvet.appointment.event.*;
import com.myvet.dataaccess.entity.Appointment;
import com.myvet.dataaccess.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Appointment createAppointment(AppointmentCreateDto dto) {
        Appointment appointment = // ... map and save
        appointmentRepository.save(appointment);
        
        // Publish event - notification module will handle it
        eventPublisher.publishEvent(new AppointmentCreatedEvent(this, appointment));
        
        return appointment;
    }

    @Transactional
    public Appointment updateAppointment(Long id, AppointmentUpdateDto dto) {
        Appointment existing = appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        Appointment previousState = // ... clone existing
        
        // ... update fields
        appointmentRepository.save(existing);
        
        // Publish event
        eventPublisher.publishEvent(new AppointmentUpdatedEvent(this, existing, previousState));
        
        return existing;
    }

    @Transactional
    public void cancelAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        
        // Publish event
        eventPublisher.publishEvent(new AppointmentCancelledEvent(this, appointment, reason));
    }
}
```

#### Event Listener

```java
// myvet-notification/src/main/java/com/myvet/notification/listener/AppointmentEventListener.java
package com.myvet.notification.listener;

import com.myvet.appointment.event.*;
import com.myvet.dataaccess.entity.Appointment;
import com.myvet.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void onAppointmentCreated(AppointmentCreatedEvent event) {
        Appointment apt = event.getAppointment();
        log.info("Handling AppointmentCreatedEvent for appointment: {}", apt.getId());

        // Notify pet owner
        String ownerSubject = "Randevu Onayı";
        String ownerMessage = String.format(
            "Merhaba! %s tarihinde saat %s için randevunuz onaylanmıştır.",
            apt.getAppointmentDate(),
            apt.getAppointmentTime()
        );
        notificationService.sendToUser(apt.getPetOwnerId(), ownerSubject, ownerMessage);

        // Notify vet
        String vetSubject = "Yeni Randevu";
        String vetMessage = String.format(
            "Yeni randevu: %s tarihinde saat %s.",
            apt.getAppointmentDate(),
            apt.getAppointmentTime()
        );
        notificationService.sendToUser(apt.getVetId(), vetSubject, vetMessage);
    }

    @Async
    @EventListener
    public void onAppointmentUpdated(AppointmentUpdatedEvent event) {
        Appointment apt = event.getAppointment();
        log.info("Handling AppointmentUpdatedEvent for appointment: {}", apt.getId());

        String subject = "Randevu Güncellendi";
        String message = String.format(
            "Randevunuz güncellendi. Yeni tarih: %s, saat: %s.",
            apt.getAppointmentDate(),
            apt.getAppointmentTime()
        );

        notificationService.sendToUser(apt.getPetOwnerId(), subject, message);
        notificationService.sendToUser(apt.getVetId(), subject, message);
    }

    @Async
    @EventListener
    public void onAppointmentCancelled(AppointmentCancelledEvent event) {
        Appointment apt = event.getAppointment();
        log.info("Handling AppointmentCancelledEvent for appointment: {}", apt.getId());

        String subject = "Randevu İptal Edildi";
        String message = String.format(
            "%s tarihindeki randevunuz iptal edilmiştir. Sebep: %s",
            apt.getAppointmentDate(),
            event.getCancellationReason()
        );

        notificationService.sendToUser(apt.getPetOwnerId(), subject, message);
        notificationService.sendToUser(apt.getVetId(), subject, message);
    }
}
```

### Why Event-Driven?

```
WITHOUT EVENTS (Tight Coupling):
─────────────────────────────────
AppointmentService
    │
    ├── appointmentRepository.save()
    ├── notificationService.send()      ← Direct dependency!
    ├── emailService.send()             ← More dependencies!
    └── smsService.send()               ← Even more!

Problem: AppointmentService knows about ALL notification details


WITH EVENTS (Loose Coupling):
─────────────────────────────────
AppointmentService
    │
    ├── appointmentRepository.save()
    └── eventPublisher.publish()        ← Only knows about events

NotificationModule listens separately

Benefits:
✅ Appointment module doesn't know notification module exists
✅ Easy to add more listeners (analytics, audit, etc.)
✅ Async processing (non-blocking)
✅ Easy to test in isolation
```

### Configuration Required

```java
// myvet-app/src/main/java/com/myvet/MyVetApplication.java
@SpringBootApplication(scanBasePackages = "com.myvet")
@EnableAsync          // Required for @Async listeners
@EnableScheduling     // Required for Option 3
public class MyVetApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyVetApplication.class, args);
    }
}
```

```yaml
# application.yml - Async thread pool config
spring:
  task:
    execution:
      pool:
        core-size: 5
        max-size: 10
        queue-capacity: 25
      thread-name-prefix: async-notification-
```

### Pros & Cons

| Pros | Cons |
|------|------|
| Fully automated | No customization per notification |
| Immediate (async) | Requires event infrastructure |
| Loose coupling | Debugging can be harder |
| Scalable | Event ordering not guaranteed |
| Easy to extend | |

---

## Option 3: Recurring Treatment Reminders

### Description

System automatically sends reminders for recurring treatments (vaccines, medications, checkups) based on scheduled due dates. A daily job checks for upcoming treatments and notifies pet owners.

### Use Cases

| Treatment Type | Interval | Example Reminder Message |
|----------------|----------|--------------------------|
| Kuduz Aşısı (Rabies) | 365 days | "Max'in kuduz aşısı 7 gün içinde yapılmalı" |
| İç Parazit | 90 days | "Mia'nın iç parazit ilacı 3 gün içinde verilmeli" |
| Dış Parazit | 30 days | "Rocky'nin dış parazit ilacı yarın verilmeli" |
| Genel Kontrol | 180 days | "Luna'nın 6 aylık kontrolü yaklaşıyor" |

### Technical Approach

**Pattern:** Scheduled Job (Cron → Query → Batch Process → Notify)

### Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│            RECURRING TREATMENT REMINDER FLOW                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   STEP 1: VET CREATES RECURRING TREATMENT                       │
│   ─────────────────────────────────────────                     │
│                                                                  │
│   POST /api/pets/{petId}/treatments                             │
│   {                                                              │
│       "name": "Kuduz Aşısı",                                    │
│       "type": "VACCINATION",                                     │
│       "date": "2025-01-28",                                     │
│       "isRecurring": true,                                      │
│       "intervalDays": 365,                                      │
│       "reminderDaysBefore": 7                                   │
│   }                                                              │
│                                                                  │
│   System calculates:                                            │
│   → nextDueDate = 2026-01-28                                    │
│   → reminderSent = false                                        │
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   STEP 2: DAILY SCHEDULER (Every day at 9:00 AM)               │
│   ─────────────────────────────────────────────                 │
│                                                                  │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  TreatmentReminderScheduler                             │   │
│   │                                                          │   │
│   │  @Scheduled(cron = "0 0 9 * * *")                       │   │
│   │  public void sendTreatmentReminders() {                 │   │
│   │      // 1. Query database                               │   │
│   │      // 2. Process each treatment                       │   │
│   │      // 3. Send notifications                           │   │
│   │      // 4. Update flags                                 │   │
│   │  }                                                       │   │
│   └─────────────────────────────────────────────────────────┘   │
│                              │                                   │
│                              ▼                                   │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  DATABASE QUERY                                          │   │
│   │                                                          │   │
│   │  SELECT * FROM recurring_treatments                      │   │
│   │  WHERE active = true                                     │   │
│   │    AND reminder_sent = false                            │   │
│   │    AND DATEDIFF(next_due_date, TODAY) <= reminder_days  │   │
│   └─────────────────────────────────────────────────────────┘   │
│                              │                                   │
│                              ▼                                   │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  FOR EACH TREATMENT                                      │   │
│   │                                                          │   │
│   │  1. Get pet and owner info                              │   │
│   │  2. Calculate days until due                            │   │
│   │  3. Build message (Turkish)                             │   │
│   │  4. Send via NotificationService                        │   │
│   │  5. Set reminderSent = true                             │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   STEP 3: APPOINTMENT COMPLETED (Cycle Reset)                   │
│   ───────────────────────────────────────────                   │
│                                                                  │
│   When vet marks treatment appointment as completed:            │
│                                                                  │
│   1. Find linked RecurringTreatment                             │
│   2. Update lastDoneDate = today                                │
│   3. Update nextDueDate = today + intervalDays                  │
│   4. Reset reminderSent = false                                 │
│                                                                  │
│   → Cycle continues automatically                               │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Components Required

| Component | Module | Description |
|-----------|--------|-------------|
| `RecurringTreatment` | `myvet-data-access` | Entity for recurring schedules |
| `TreatmentType` | `myvet-data-access` | Enum for treatment types |
| `RecurringTreatmentRepository` | `myvet-data-access` | Query upcoming treatments |
| `TreatmentService` | `myvet-pet` | CRUD + auto-calculate dates |
| `TreatmentReminderScheduler` | `myvet-notification` | Daily cron job |
| `NotificationService` | `myvet-notification` | Sends notifications |

### File Structure

```
myvet-data-access/
├── entity/
│   ├── RecurringTreatment.java
│   └── TreatmentType.java (enum)
└── repository/
    └── RecurringTreatmentRepository.java

myvet-pet/
├── dto/
│   ├── TreatmentCreateDto.java
│   └── TreatmentDto.java
├── controller/
│   └── TreatmentController.java
└── service/
    └── TreatmentService.java

myvet-notification/
└── scheduler/
    └── TreatmentReminderScheduler.java
```

### Code Examples

#### Enum

```java
// myvet-data-access/src/main/java/com/myvet/dataaccess/entity/TreatmentType.java
package com.myvet.dataaccess.entity;

public enum TreatmentType {
    VACCINATION,    // Aşı
    MEDICATION,     // İlaç
    CHECKUP,        // Kontrol
    PARASITE,       // Parazit
    DENTAL,         // Diş
    OTHER           // Diğer
}
```

#### Entity

```java
// myvet-data-access/src/main/java/com/myvet/dataaccess/entity/RecurringTreatment.java
package com.myvet.dataaccess.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_treatments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTreatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationships
    private Long petId;
    private Long vetId;

    // Treatment info
    @Enumerated(EnumType.STRING)
    private TreatmentType type;

    private String name;  // "Kuduz Aşısı", "İç Parazit"

    private String notes;

    // Schedule
    private Integer intervalDays;     // 365, 90, 30
    private LocalDate lastDoneDate;
    private LocalDate nextDueDate;    // Auto-calculated

    // Reminder settings
    @Builder.Default
    private Integer reminderDaysBefore = 7;

    @Builder.Default
    private boolean reminderSent = false;

    // Status
    @Builder.Default
    private boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastDoneDate != null && intervalDays != null) {
            nextDueDate = lastDoneDate.plusDays(intervalDays);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Reset for next cycle after treatment completed
     */
    public void completeTreatment(LocalDate completedDate) {
        this.lastDoneDate = completedDate;
        this.nextDueDate = completedDate.plusDays(this.intervalDays);
        this.reminderSent = false;
    }
}
```

#### Repository

```java
// myvet-data-access/src/main/java/com/myvet/dataaccess/repository/RecurringTreatmentRepository.java
package com.myvet.dataaccess.repository;

import com.myvet.dataaccess.entity.RecurringTreatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTreatmentRepository extends JpaRepository<RecurringTreatment, Long> {

    /**
     * Find treatments where:
     * - active = true
     * - reminderSent = false
     * - (nextDueDate - reminderDaysBefore) <= today
     */
    @Query("""
        SELECT t FROM RecurringTreatment t 
        WHERE t.active = true 
          AND t.reminderSent = false 
          AND t.nextDueDate IS NOT NULL
          AND FUNCTION('DATEDIFF', t.nextDueDate, :today) <= t.reminderDaysBefore
        """)
    List<RecurringTreatment> findTreatmentsDueForReminder(@Param("today") LocalDate today);

    /**
     * Alternative: Native SQL query
     */
    @Query(value = """
        SELECT * FROM recurring_treatments t 
        WHERE t.active = true 
          AND t.reminder_sent = false 
          AND t.next_due_date IS NOT NULL
          AND DATEDIFF(t.next_due_date, :today) <= t.reminder_days_before
        """, nativeQuery = true)
    List<RecurringTreatment> findTreatmentsDueForReminderNative(@Param("today") LocalDate today);

    List<RecurringTreatment> findByPetId(Long petId);

    List<RecurringTreatment> findByPetIdAndActiveTrue(Long petId);

    List<RecurringTreatment> findByVetId(Long vetId);
}
```

#### Scheduler

```java
// myvet-notification/src/main/java/com/myvet/notification/scheduler/TreatmentReminderScheduler.java
package com.myvet.notification.scheduler;

import com.myvet.dataaccess.entity.Pet;
import com.myvet.dataaccess.entity.RecurringTreatment;
import com.myvet.dataaccess.repository.PetRepository;
import com.myvet.dataaccess.repository.RecurringTreatmentRepository;
import com.myvet.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TreatmentReminderScheduler {

    private final RecurringTreatmentRepository treatmentRepository;
    private final PetRepository petRepository;
    private final NotificationService notificationService;

    /**
     * Runs every day at 9:00 AM Istanbul time
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Europe/Istanbul")
    @Transactional
    public void sendTreatmentReminders() {
        log.info("Starting treatment reminder job...");

        LocalDate today = LocalDate.now();
        List<RecurringTreatment> treatments = treatmentRepository
                .findTreatmentsDueForReminder(today);

        log.info("Found {} treatments needing reminders", treatments.size());

        int successCount = 0;
        int failCount = 0;

        for (RecurringTreatment treatment : treatments) {
            try {
                sendReminderForTreatment(treatment, today);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send reminder for treatment {}: {}",
                        treatment.getId(), e.getMessage());
                failCount++;
            }
        }

        log.info("Treatment reminder job completed. Success: {}, Failed: {}",
                successCount, failCount);
    }

    private void sendReminderForTreatment(RecurringTreatment treatment, LocalDate today) {
        // Get pet info
        Pet pet = petRepository.findById(treatment.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet not found: " + treatment.getPetId()));

        // Calculate days until due
        long daysUntilDue = ChronoUnit.DAYS.between(today, treatment.getNextDueDate());

        // Build message
        String subject = "Tedavi Hatırlatması - " + pet.getName();
        String message = buildReminderMessage(pet, treatment, daysUntilDue);

        // Send to pet owner
        notificationService.sendToUser(pet.getOwnerId(), subject, message);

        // Mark reminder as sent
        treatment.setReminderSent(true);
        treatmentRepository.save(treatment);

        log.info("Sent reminder for pet {} - treatment {} (due in {} days)",
                pet.getName(), treatment.getName(), daysUntilDue);
    }

    private String buildReminderMessage(Pet pet, RecurringTreatment treatment, long daysUntilDue) {
        String petName = pet.getName();
        String treatmentName = treatment.getName();

        if (daysUntilDue < 0) {
            return String.format(
                "⚠️ Dikkat! %s adlı dostunuzun %s tedavisi %d gün önce yapılmalıydı. " +
                "Lütfen en kısa sürede randevu oluşturun.",
                petName, treatmentName, Math.abs(daysUntilDue)
            );
        } else if (daysUntilDue == 0) {
            return String.format(
                "📅 Bugün! %s adlı dostunuzun %s tedavisi bugün yapılmalı. " +
                "Randevunuz var mı?",
                petName, treatmentName
            );
        } else if (daysUntilDue == 1) {
            return String.format(
                "⏰ Yarın! %s adlı dostunuzun %s tedavisi yarın yapılmalı. " +
                "Randevu oluşturmayı unutmayın!",
                petName, treatmentName
            );
        } else if (daysUntilDue <= 3) {
            return String.format(
                "🔔 %s adlı dostunuzun %s tedavisi %d gün içinde yapılmalı. " +
                "Şimdiden randevu oluşturmanızı öneririz.",
                petName, treatmentName, daysUntilDue
            );
        } else {
            return String.format(
                "📋 Hatırlatma: %s adlı dostunuzun %s tedavisi %d gün içinde yapılmalı. " +
                "Uygun bir zamanda randevu oluşturabilirsiniz.",
                petName, treatmentName, daysUntilDue
            );
        }
    }
}
```

#### Treatment Service (Cycle Reset)

```java
// myvet-pet/src/main/java/com/myvet/pet/service/TreatmentService.java
package com.myvet.pet.service;

import com.myvet.dataaccess.entity.RecurringTreatment;
import com.myvet.dataaccess.repository.RecurringTreatmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TreatmentService {

    private final RecurringTreatmentRepository treatmentRepository;

    /**
     * Called when appointment for this treatment is completed
     * Resets the cycle for next reminder
     */
    @Transactional
    public void completeTreatment(Long treatmentId) {
        RecurringTreatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));

        treatment.completeTreatment(LocalDate.now());
        treatmentRepository.save(treatment);
    }

    @Transactional
    public RecurringTreatment createRecurringTreatment(TreatmentCreateDto dto) {
        RecurringTreatment treatment = RecurringTreatment.builder()
                .petId(dto.getPetId())
                .vetId(dto.getVetId())
                .type(dto.getType())
                .name(dto.getName())
                .notes(dto.getNotes())
                .intervalDays(dto.getIntervalDays())
                .lastDoneDate(dto.getDate())
                .nextDueDate(dto.getDate().plusDays(dto.getIntervalDays()))
                .reminderDaysBefore(dto.getReminderDaysBefore() != null ? dto.getReminderDaysBefore() : 7)
                .reminderSent(false)
                .active(true)
                .build();

        return treatmentRepository.save(treatment);
    }
}
```

### Cron Expression Reference

```
@Scheduled(cron = "0 0 9 * * *")
                   │ │ │ │ │ │
                   │ │ │ │ │ └── Day of week (0-7 or SUN-SAT)
                   │ │ │ │ └──── Month (1-12)
                   │ │ │ └────── Day of month (1-31)
                   │ │ └──────── Hour (0-23)
                   │ └────────── Minute (0-59)
                   └──────────── Second (0-59)

Common Examples:
────────────────
"0 0 9 * * *"       → Every day at 09:00:00
"0 30 8 * * *"      → Every day at 08:30:00
"0 0 9 * * MON-FRI" → Weekdays at 09:00:00
"0 0 */2 * * *"     → Every 2 hours
"0 0 9,18 * * *"    → Every day at 09:00 and 18:00
```

### Query Logic Example

```
TODAY: 2026-01-21

TREATMENT DATA:
┌────┬─────────────┬──────────────┬─────────────────┬──────────────┐
│ ID │ Name        │ nextDueDate  │ reminderDays    │ reminderSent │
├────┼─────────────┼──────────────┼─────────────────┼──────────────┤
│ 1  │ Kuduz Aşısı │ 2026-01-28   │ 7               │ false        │
│ 2  │ İç Parazit  │ 2026-01-25   │ 7               │ false        │
│ 3  │ Dış Parazit │ 2026-02-15   │ 7               │ false        │
│ 4  │ Kontrol     │ 2026-01-22   │ 3               │ true         │
└────┴─────────────┴──────────────┴─────────────────┴──────────────┘

QUERY RESULT:
─────────────
ID 1: 2026-01-28 - 2026-01-21 = 7 days  ≤ 7 → INCLUDE ✅
ID 2: 2026-01-25 - 2026-01-21 = 4 days  ≤ 7 → INCLUDE ✅
ID 3: 2026-02-15 - 2026-01-21 = 25 days > 7 → EXCLUDE ❌
ID 4: reminderSent = true                   → EXCLUDE ❌

RESULT: [Treatment 1, Treatment 2]
```

### Pros & Cons

| Pros | Cons |
|------|------|
| Fully automated | Not real-time |
| No manual intervention | Batch processing delay |
| Consistent timing | Requires scheduler management |
| Handles recurring logic | More complex setup |
| Scalable | Database load during job |

---

## Shared Components

All three options share these core components:

### File Structure

```
myvet-notification/
├── dto/
│   └── NotificationRequest.java
├── provider/
│   ├── NotificationProvider.java (interface)
│   ├── EmailProvider.java
│   ├── WhatsAppProvider.java
│   ├── PushNotificationProvider.java
│   └── SmsProvider.java
└── service/
    └── NotificationService.java

myvet-data-access/
├── entity/
│   ├── NotificationLog.java
│   ├── NotificationPreference.java
│   ├── NotificationType.java
│   └── NotificationStatus.java
└── repository/
    ├── NotificationLogRepository.java
    └── NotificationPreferenceRepository.java
```

### Enums

```java
// myvet-data-access/src/main/java/com/myvet/dataaccess/entity/NotificationType.java
package com.myvet.dataaccess.entity;

public enum NotificationType {
    EMAIL,
    SMS,
    WHATSAPP,
    PUSH
}
```

```java
// myvet-data-access/src/main/java/com/myvet/dataaccess/entity/NotificationStatus.java
package com.myvet.dataaccess.entity;

public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    DELIVERED
}
```

### Entities

```java
// myvet-data-access/src/main/java/com/myvet/dataaccess/entity/NotificationLog.java
package com.myvet.dataaccess.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String recipient;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private String errorMessage;

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

```java
// myvet-data-access/src/main/java/com/myvet/dataaccess/entity/NotificationPreference.java
package com.myvet.dataaccess.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private boolean enabled;

    private String destination;  // email, phone, device token
}
```

### Provider Interface

```java
// myvet-notification/src/main/java/com/myvet/notification/provider/NotificationProvider.java
package com.myvet.notification.provider;

import com.myvet.dataaccess.entity.NotificationType;
import com.myvet.notification.dto.NotificationRequest;

public interface NotificationProvider {
    
    NotificationType getType();
    
    void send(NotificationRequest request);
    
    boolean isEnabled();
}
```

### Notification Service

```java
// myvet-notification/src/main/java/com/myvet/notification/service/NotificationService.java
package com.myvet.notification.service;

import com.myvet.dataaccess.entity.*;
import com.myvet.dataaccess.repository.NotificationLogRepository;
import com.myvet.dataaccess.repository.NotificationPreferenceRepository;
import com.myvet.notification.dto.NotificationRequest;
import com.myvet.notification.provider.NotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final Map<NotificationType, NotificationProvider> providers;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationLogRepository logRepository;

    public NotificationService(
            List<NotificationProvider> providerList,
            NotificationPreferenceRepository preferenceRepository,
            NotificationLogRepository logRepository
    ) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(
                        NotificationProvider::getType,
                        Function.identity()
                ));
        this.preferenceRepository = preferenceRepository;
        this.logRepository = logRepository;
    }

    /**
     * Send notification to user through all their enabled channels
     */
    public void sendToUser(Long userId, String subject, String message) {
        List<NotificationPreference> preferences =
                preferenceRepository.findByUserIdAndEnabledTrue(userId);

        if (preferences.isEmpty()) {
            log.warn("No notification preferences found for user: {}", userId);
            return;
        }

        for (NotificationPreference pref : preferences) {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(userId)
                    .type(pref.getType())
                    .recipient(pref.getDestination())
                    .subject(subject)
                    .message(message)
                    .build();

            send(request);
        }
    }

    /**
     * Send single notification
     */
    public void send(NotificationRequest request) {
        NotificationProvider provider = providers.get(request.getType());

        if (provider == null) {
            log.warn("No provider found for type: {}", request.getType());
            return;
        }

        if (!provider.isEnabled()) {
            log.info("Provider {} is disabled, skipping", request.getType());
            return;
        }

        NotificationLog logEntry = NotificationLog.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(NotificationStatus.PENDING)
                .build();

        try {
            provider.send(request);
            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setSentAt(LocalDateTime.now());
            log.info("Notification sent: type={}, recipient={}",
                    request.getType(), request.getRecipient());
        } catch (Exception e) {
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setErrorMessage(e.getMessage());
            log.error("Failed to send notification: type={}, recipient={}, error={}",
                    request.getType(), request.getRecipient(), e.getMessage());
        }

        logRepository.save(logEntry);
    }
}
```

---

## Architecture Summary

### Module Dependency Graph

```
┌─────────────────────────────────────────────────────────────────┐
│                    MODULE DEPENDENCY GRAPH                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│                      myvet-common                               │
│                           │                                      │
│                           ▼                                      │
│                    myvet-data-access                            │
│                           │                                      │
│              ┌────────────┼────────────┐                        │
│              │            │            │                         │
│              ▼            ▼            ▼                         │
│         myvet-auth   myvet-pet   myvet-vet                      │
│              │            │            │                         │
│              └────────────┼────────────┘                        │
│                           │                                      │
│                           ▼                                      │
│                   myvet-appointment                             │
│                           │                                      │
│                           │ publishes events                    │
│                           ▼                                      │
│                   myvet-notification                            │
│                           │                                      │
│                           │ • listens to events                 │
│                           │ • runs schedulers                   │
│                           │ • exposes manual endpoints          │
│                           │                                      │
│                           ▼                                      │
│                      myvet-app                                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flow Comparison

| Aspect | Option 1 (Manual) | Option 2 (Events) | Option 3 (Scheduler) |
|--------|-------------------|-------------------|----------------------|
| **Trigger** | HTTP Request | Domain Event | Cron Job |
| **Timing** | Immediate | Immediate (async) | Scheduled (batch) |
| **Initiator** | Vet | System | System |
| **Pattern** | Request/Response | Pub/Sub | Polling |
| **Coupling** | Direct | Loose | Loose |
| **Processing** | Sync | Async | Batch |
| **Spring Feature** | @RestController | @EventListener + @Async | @Scheduled |

### Configuration Required

```java
// myvet-app/src/main/java/com/myvet/MyVetApplication.java
@SpringBootApplication(scanBasePackages = "com.myvet")
@EnableAsync          // Required for Option 2 (async event listeners)
@EnableScheduling     // Required for Option 3 (scheduled jobs)
public class MyVetApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyVetApplication.class, args);
    }
}
```

```yaml
# myvet-app/src/main/resources/application.yml

# Async configuration
spring:
  task:
    execution:
      pool:
        core-size: 5
        max-size: 10
        queue-capacity: 25
      thread-name-prefix: async-notification-

# Notification settings
notification:
  email:
    enabled: true
    from: noreply@myvet.com
  sms:
    enabled: false
  whatsapp:
    enabled: false
  push:
    enabled: false

# Scheduler timezone
  scheduling:
    timezone: Europe/Istanbul
```

---

## Implementation Priority

### Recommended Phases

```
PHASE 1: Foundation (Week 1)
────────────────────────────
✅ NotificationService
✅ NotificationProvider interface
✅ EmailProvider (basic implementation)
✅ NotificationLog entity
✅ NotificationPreference entity
✅ Repositories

PHASE 2: Manual Notifications - Option 1 (Week 2)
─────────────────────────────────────────────────
✅ SendNotificationRequest DTO
✅ NotificationController
✅ Security (vet authorization)
✅ Testing

PHASE 3: Appointment Events - Option 2 (Week 3)
───────────────────────────────────────────────
✅ Event classes (Created, Updated, Cancelled)
✅ Event publishing in AppointmentService
✅ AppointmentEventListener
✅ @EnableAsync configuration
✅ Testing

PHASE 4: Recurring Treatments - Option 3 (Week 4)
─────────────────────────────────────────────────
✅ RecurringTreatment entity
✅ RecurringTreatmentRepository
✅ TreatmentService integration
✅ TreatmentReminderScheduler
✅ Appointment completion → cycle reset
✅ Testing

PHASE 5: Additional Providers (Week 5+)
───────────────────────────────────────
⬜ WhatsApp integration (Twilio/Meta API)
⬜ Push notification (Firebase FCM)
⬜ SMS integration (Twilio)
⬜ Notification templates
```

### Effort Estimation

| Component | Complexity | Estimated Time |
|-----------|------------|----------------|
| Shared Infrastructure | Medium | 3-4 days |
| Option 1 (Manual) | Low | 2-3 days |
| Option 2 (Events) | Medium | 3-4 days |
| Option 3 (Scheduler) | Medium-High | 4-5 days |
| **Total** | | **2-3 weeks** |

---

## Summary

### Quick Reference Table

| Feature | Option 1 | Option 2 | Option 3 |
|---------|----------|----------|----------|
| **Name** | Manual | Appointment Events | Recurring Treatments |
| **Pattern** | REST API | Event-Driven | Scheduled Job |
| **Trigger** | Vet action | CRUD operations | Daily cron |
| **Spring Feature** | @RestController | @EventListener + @Async | @Scheduled |
| **Module** | notification | appointment → notification | notification |
| **Real-time** | ✅ Yes | ✅ Yes (async) | ❌ No (batch) |
| **Automation** | ❌ Manual | ✅ Full | ✅ Full |
| **Use Case** | Custom messages | Booking lifecycle | Treatment reminders |
| **Complexity** | Low | Medium | Medium-High |

### Key Takeaways

1. **Option 1 (Manual):** Simple REST endpoint, vet has full control, immediate delivery
2. **Option 2 (Events):** Loose coupling via Spring Events, automatic on appointment changes
3. **Option 3 (Scheduler):** Daily batch job for recurring treatments, handles vaccine/medication reminders

All three options share the same `NotificationService` and `NotificationProvider` infrastructure, making the system extensible and maintainable.

---

*Document prepared for MyVet Development Team*  
*Last updated: January 2025*
