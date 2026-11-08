
# PetDetective
PetDetective es una aplicación móvil nativa para Android diseñada para ayudar a la comunidad a reportar, buscar y encontrar mascotas perdidas o encontradas en tiempo real.

## Características Principales

- **Reportes en Tiempo Real:** Crea reportes de mascotas perdidas o encontradas con foto, ubicación y datos de contacto.
- **Geolocalización:** Captura de coordenadas GPS exactas y visualización en mapas interactivos (Google Maps).
- **Filtros y Búsqueda:** Filtra reportes por especie (Perro, Gato, Ave), estado (Perdido/Encontrado) o usa el filtro "Cerca de mí" para ver mascotas en un radio de 10km.
- **Panel de Control (Dashboard):** Estadísticas en vivo y acceso rápido a los últimos reportes.
- **Seguridad:** Autenticación anónima con Firebase para proteger la base de datos contra spam.
- **Modo Offline:** Caché local con Firestore para seguir viendo reportes sin conexión a internet.

## Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **Interfaz de Usuario:** Jetpack Compose (Material Design 3)
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Backend / Base de Datos:** Firebase Cloud Firestore
- **Autenticación:** Firebase Authentication (Anónimo)
- **Mapas:** Google Maps SDK for Android
- **Carga de Imágenes:** Coil
- **Persistencia Local (histórico):** Room (SQLite)

## Seguridad y Reglas de Firebase

El proyecto utiliza reglas de seguridad en Firestore para garantizar la integridad de los datos:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /pet_reports/{document} {
      allow read: if true;
      allow create, delete: if request.auth != null;
      allow update: if false;
    }
  }
}