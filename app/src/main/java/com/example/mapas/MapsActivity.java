package com.example.mapas;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{


    private GoogleMap mMap;
    private static final int LOCATION_REQUEST_CODE = 1; // Para los permisos
    private boolean permisos = false;

    // Para obtener el punto actual (no es necesario para el mapa)
    // Pero si para obtener las latitud y la longitud
    private FusedLocationProviderClient mPosicion;

    private Location miUltimaLocalizacion;
    private LatLng posDefecto = new LatLng(38.6901212, -4.1086075);
    private LatLng posActual = posDefecto;

    // Marcador actual
    private Marker marcadorActual = null;

    // Marcador marcadorTouch
    private Marker marcadorTouch = null;

    // Posición actual con eventos y no hilos
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int  CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000 ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Para obtener las coordenadas de la posición actual
        // s decir lleer nosotros manualmente el GPS
        // No es necesario para pintar la bola azul
        // Construct a FusedLocationProviderClient.
        mPosicion = LocationServices.getFusedLocationProviderClient(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Solicitamos prmisos de Localización
        solicitarPermisos();


        // Configurar IU Mapa
        configurarIUMapa();


        // Añadmimos marcadores
        añadirMarcadores();

        // activa el evento de marcadores Touc
        activarEventosMarcdores();

        // Obtenemos la posición GPS
        // Esto lo hago para informar de la última posición
        // Obteniendo coordenadas GPS directamente
        obtenerPosicion();

        // Situar la camara inicialmente a una posición determinada
        situarCamaraMapa();


        // Acrtualizar cada X Tiempo, implica programar eventos o hacerlo con un hilo
        // Esto consume, por lo que ideal es activarlo y desactivarlo
        // cuando sea necesario
        //autoActualizador();


        // Para usar eventos
        // Yo lo haría con obtenerposición, pues hacemos lo mismo
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Crear el LocationRequest
        // Es muy similar a lo que yo he hecho manualmente con el reloj en     private void autoActualizador {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 segundos en milisegundos
                .setFastestInterval(1 * 1000); // 1 segundo en milisegundos




    }

    private void activarEventosMarcdores() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                // Creamos el marcador
                // Borramos el marcador Touch si está puesto
                if(marcadorTouch!=null){
                    marcadorTouch.remove();
                }
                marcadorTouch=mMap.addMarker(new MarkerOptions()
                        // Posición
                        .position(point)
                        // Título
                        .title("Marcador Touch")
                        // Subtitulo
                        .snippet("El Que tú has puesto")
                        // Color o tipo d icono
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                );
                mMap.moveCamera(CameraUpdateFactory.newLatLng(point));

            }
        });



    }

    private void situarCamaraMapa() {
        // Puedo moverla a una posición que queramos
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(posDefecto));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posActual));
    }

    private void configurarIUMapa() {
        // Puedo activar eventos para un solo marcador o varios
        // Con la interfaz  OnMarkerClickListener
        mMap.setOnMarkerClickListener(this);

        // Activar Boton de Posición actual
        if(permisos){
            // Si tenemos permisos pintamos el botón de la localización actual
            // Esta posición la obtiene google automaticamente y no tiene que ver con
            // obtener posición
            mMap.setMyLocationEnabled(true);
        }

        // Mapa híbrido, lo normal es usar el
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Que se vea la interfaz y la brújula por ejemplo
        // También podemos quitar gestos
        UiSettings uiSettings = mMap.getUiSettings();
        // Activamos los gestos
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(true);
        // Activamos la brújula
        uiSettings.setCompassEnabled(true);
        // Activamos los controles de zoom
        uiSettings.setZoomControlsEnabled(true);
        // Activamos la brújula
        uiSettings.setCompassEnabled(true);
        // Actiovamos la barra de herramientas
        uiSettings.setMapToolbarEnabled(true);

        // Hacemos el zoom por defecto mínimo
        mMap.setMinZoomPreference(15.0f);
        // Señalamos el tráfico
        mMap.setTrafficEnabled(true);
    }

    private void añadirMarcadores() {
        // Podemos lerlos de cualquier sitios
        // Añadimos un marcador en la estación
        // Podemos leerlos de un servición, BD SQLite, XML, Arraylist, etc
        LatLng estacion = new LatLng(38.69128, -4.111655);
        mMap.addMarker(new MarkerOptions()
                        // Posición
                        .position(estacion)
                        // Título
                        .title("Estación de AVE")
                        // Subtitulo
                        .snippet("Estación AVE de Puertollano")
                        // Color o tipo d icono
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ayuntamiento))
        );

        // Añadimos el ayuntamiento
        // Añadimos un marcador en la estación
        LatLng ayto = new LatLng(38.6866069, -4.1110002);
        mMap.addMarker(new MarkerOptions()
                        // Posición
                        .position(ayto)
                        // Título
                        .title("Ayuntamiento")
                        // Subtitulo
                        .snippet("Ayuntamiento de Puertollano")
                        // Color o tipo d icono
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ayuntamiento))
        );
    }

    // Evento de procesar o hacer click en un marker
    @Override
    public boolean onMarkerClick(Marker marker) {
        // Si pulsas ayunatmiento, si muestro el toast si no nada
        String titulo = marker.getTitle();
        switch (titulo) {
            case "Ayuntamiento":
                Toast.makeText(this,
                        marker.getTitle() +
                                " Mal sitio para ir.",
                        Toast.LENGTH_SHORT).show();
                break;

            case "Estación de AVE":
                Toast.makeText(this,
                        marker.getTitle() +
                                " Corre que pierdes el tren.",
                        Toast.LENGTH_SHORT).show();
                break;
            case "Marcador Touch":
                Toast.makeText(this,"Estás en: " + marker.getPosition().latitude+","+marker.getPosition().longitude,
                        Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
        return false;
    }

    // Obtenermos y leemos directamente el GPS
    // Esto se puede hacer trabajemos con mapas o no
    // Por ejemplo pata mostrar la localización en etiquetas
    private void obtenerPosicion() {
        try {
            if (permisos) {
                // Lo lanzamos como tarea concurrente
                Task<Location> local = mPosicion.getLastLocation();
                local.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Actualizamos la última posición conocida
                            miUltimaLocalizacion = task.getResult();
                            posActual = new LatLng(miUltimaLocalizacion.getLatitude(),
                                    miUltimaLocalizacion.getLongitude());
                            // Añadimos un marcador especial para poder operar con esto
                            marcadorPosicionActual();
                            informarPosocion();

                        } else {
                            Log.d("GPS", "No se encuetra la última posición.");
                            Log.e("GPS", "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // Para dibujar el marcador actual
    private void marcadorPosicionActual() {
        /*
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(this.posActual)
                .radius(50)
                .strokeColor(Color.MAGENTA)
                .fillColor(Color.BLUE));

        */

        // Borramos el arcador actual si está puesto
        if(marcadorActual!=null){
            marcadorActual.remove();
        }
        // añadimos el marcador actual
        marcadorActual= mMap.addMarker(new MarkerOptions()
                // Posición
                .position(posActual)
                // Título
                .title("Mi Localización")
                // Subtitulo
                .snippet("Localización actual")
                // Color o tipo d icono
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );

    }

    private void informarPosocion() {

        Toast.makeText(this,
                "Ultima posición Conocida:\n - Latitid: " + miUltimaLocalizacion.getLatitude()+ "\n- Logitud: " + miUltimaLocalizacion.getLongitude()
                        + "\n- Altura: " +miUltimaLocalizacion.getAltitude() + "\n- Precisón: " + miUltimaLocalizacion.getAccuracy()
                ,
                Toast.LENGTH_SHORT).show();
    }

    // solicitamos los permisos para leer de algo
    // Esto debemos hacerlo con todo
    public void solicitarPermisos(){
        // Antes de nada si queremos mostrar nuestra localización debemos hacer esto
        // Si tenemos los permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Activamos el botón de lalocalización
            permisos = true;
        } else {
            // Si no
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostrar diálogo explicativo
            } else {
                // Solicitar permiso
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_CODE);
            }
        }
    }

    // Para los permisos, implementamos este método
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permisos = false;
        if (requestCode == LOCATION_REQUEST_CODE) {
            // ¿Permisos asignados?
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permisos = true;
            } else {
                Toast.makeText(this, "Error de permisos", Toast.LENGTH_LONG).show();
            }
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permisos = true;
            } else {
                Toast.makeText(this, "Error de permisos", Toast.LENGTH_LONG).show();
            }

        }
    }

    // Hilo con un reloj interno
    // Te acuerdas de los problemas de PSP con un Thread.Sleep()
    // Aqui lo llevas
    private void autoActualizador() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsyncTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Obtenemos la posición
                            obtenerPosicion();
                            situarCamaraMapa();

                        } catch (Exception e) {
                            Log.e("TIMER", "Error: "+e.getMessage());
                        }
                    }
                });
            }


        };
        // Actualizamos cada 10 segundos
        // podemos pararlo con timer.cancel();
        timer.schedule(doAsyncTask, 0, 10000);
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        Log.d("Mapa", location.toString());
        miUltimaLocalizacion = location;
        posActual = new LatLng(miUltimaLocalizacion.getLatitude(),
                miUltimaLocalizacion.getLongitude());
        // Añadimos un marcador especial para poder operar con esto
        marcadorPosicionActual();
        informarPosocion();
        situarCamaraMapa();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i("Mapa", "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }
}
