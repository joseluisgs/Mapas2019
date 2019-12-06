# Mapas2019
Mapas 2DAM 2019-2020. Ejemplo de Usos y abusos

V.2.0 Actualizado a API29
No olvides añadir esto a tu Gradle
    // Para mapas
    implementation 'com.google.android.gms:play-services-maps:17.0.0'
    // Para localización
    implementation 'com.google.android.gms:play-services-location:17.0.0'
    

Coloca una coordenada en un mapa de google.
Cosas a tener en cuenta, se debe generar una API Key. En el Manifest
<meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="@string/google_maps_key" />

En el modo debug se hace en ese fichero con la huella SHA-1 y s epone, en el Modo Release,
se debe generar con keytool la huella SHA-1 con los datos del paquete Release, creaer un proyecto y subirla
https://developers.google.com/maps/documentation/android-sdk/get-api-key

SI NO VES EL MAPA ES POR ESO

Se ha seguido este tutorial aproximadamente
https://developers.google.com/maps/documentation/android-sdk/map-with-marker

Importante que en buesto Gradle tengáis como mínimo
implementation 'com.google.android.gms:play-services-maps:16.1.0'

Añadir mi ubucación
Manifest
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
En caso de que tu target sea Android Marshmallow hacia adelante debes preguntarle al usuario por los permisos en tiempo de ejecución.

Otros enlaces de interes
Dicumetación oficial
https://developer.android.com/training/maps?hl=es-419
https://developers.google.com/android/reference/com/google/android/gms/maps/UiSettings
https://developers.google.com/maps/documentation/android-sdk/marker
https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial

http://www.androidcurso.com/index.php/tutoriales-android/41-unidad-7-seguridad-y-posicionamiento/223-google-maps-api-v2

Resumen muy completo
http://www.hermosaprogramacion.com/2016/05/google-maps-android-api-v2/

Permisos
https://developer.android.com/training/permissions/requesting.html

Otros
https://stackoverflow.com/questions/13742551/how-to-get-my-location-changed-event-with-google-maps-android-api-v2

