🏥 Sistema de Gestión de Pacientes Hospital Universitario

Este es un proyecto de aplicación móvil nativa (Android) diseñado para simular un sistema básico de gestión de historiales de pacientes para un entorno hospitalario. Permite a los usuarios (médicos o personal administrativo) registrar y actualizar información crucial de los pacientes y sus visitas médicas.

✨ Características Principales

Registro de Pacientes: Almacena datos básicos del paciente, incluyendo nombre, edad, y género.

Historial Médico Detallado: Permite registrar y editar información específica de la visita, como:

Diagnóstico.

Número de habitación.

Fecha de la visita (con selección de calendario).

Médico tratante.

Ubicación del hospital.

Gestión en Tiempo Real (Firestore): Los datos son persistentes y se actualizan en tiempo real gracias a Firebase Firestore.

Integración con Calendario: Función para agregar citas de pacientes directamente al calendario del dispositivo (Android Calendar Provider), facilitando la gestión de la agenda médica.

Validación de Datos: Incluye lógica de validación, como la verificación de la fecha de visita.

🛠️ Tecnología y Herramientas

Plataforma: Android (Java).

Base de Datos (BaaS): Firebase Firestore para el almacenamiento escalable y en tiempo real de los historiales de pacientes.

Autenticación: Firebase Authentication para la gestión de usuarios y permisos (asumiendo que se requiere acceso autenticado para el personal).

Librerías/APIs clave:

Android SDK y Gradle.

Calendar Provider API (para la función de añadir citas).

📄 Estructura de la Base de Datos (Firestore)

Los datos de los pacientes se almacenan en una colección pública bajo el siguiente esquema:

/artifacts/{appId}/public/data/patients/{patientUid}
    |-- name: "Nombre del Paciente"
    |-- age: "Edad"
    |-- gender: "Género"
    |-- diagnosis: "Diagnóstico de la Visita"
    |-- roomNumber: "Número de Habitación"
    |-- visitDate: "AAAA-MM-DD"
    |-- attendingDoctor: "Nombre del Doctor"
    |-- hospitalLocationAddress: "Dirección del Hospital"
