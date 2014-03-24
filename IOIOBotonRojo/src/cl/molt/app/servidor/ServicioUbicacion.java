package cl.molt.app.servidor;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.text.SimpleDateFormat;

import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;


public class ServicioUbicacion extends Service  implements LocationListener{
	

	
	protected LocationManager locationManager;
	//protected LocationListener locationListener;
	
	static final String TAG = "Mensaje -"; 
	static final String patente = "RA7097"; 
	
    public class MyBinder extends Binder{
        ServicioUbicacion getService(){
            return ServicioUbicacion.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
    	
        Toast.makeText(this,"service destroyed",Toast.LENGTH_SHORT).show();
        locationManager.removeUpdates(this);
        super.onDestroy();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        //Toast.makeText(this,"task perform in service",Toast.LENGTH_SHORT).show();
        
        //actualizarPosicion();
        
        return super.onStartCommand(intent, flags, startId);
    }


    public void onLocationChanged(Location location) {

		TareaUbicacion tarea = new TareaUbicacion();
		tarea.execute(patente, String.valueOf(location.getLatitude()), 
				               String.valueOf(location.getLongitude()),
				               String.valueOf(location.getAccuracy())
				               );

    }

    public void onProviderDisabled(String provider) {
    	Log.d(TAG, " - Registro ubicacion disabled.");

    }

    public void onProviderEnabled(String provider) {
    	Log.d(TAG, " - Registro ubicacion enabled.");

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    	
    	Log.d(TAG, " - Registro ubicacion changed.");

    }

	/*@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnActualizar = (Button)findViewById(R.id.BtnActualizar);
        btnDesactivar = (Button)findViewById(R.id.BtnDesactivar);
        lblLatitud = (TextView)findViewById(R.id.LblPosLatitud);
        lblLongitud = (TextView)findViewById(R.id.LblPosLongitud);
        lblPrecision = (TextView)findViewById(R.id.LblPosPrecision);
        lblEstado = (TextView)findViewById(R.id.LblEstado);
        
        btnActualizar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				actualizarPosicion();
				setServiceWakeUpTime();
			}
		});
        
        btnDesactivar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	locationManager.removeUpdates(locationListener);
			}
		});
	}*/
    /*@Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
 
        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        //Toast.makeText(this,"task perform in service",Toast.LENGTH_SHORT).show();
        
        //actualizarPosicion();
        
        return super.onStartCommand(intent, flags, startId);
    }
		
    private void actualizarPosicion()
    {
    	//Obtenemos una referencia al LocationManager
    	locationManager = 
    		(LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	
    	//Obtenemos la �ltima posici�n conocida
    	Location location = 
    		locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    	
    	//Mostramos la �ltima posici�n conocida
    	//muestraPosicion(location);
    	
    	//Nos registramos para recibir actualizaciones de la posici�n
    	locationListener = new LocationListener() {
	    	public void onLocationChanged(Location location) {
	    		
	    		TareaUbicacion tarea = new TareaUbicacion();
	    		tarea.execute(patente, String.valueOf(location.getLatitude()), 
	    				               String.valueOf(location.getLongitude()),
	    				               String.valueOf(location.getAccuracy())
	    				               );
	    		
	    		//muestraPosicion(location);
	    	}
	    	public void onProviderDisabled(String provider){
	    		//lblEstado.setText("Provider OFF");
	    	}
	    	public void onProviderEnabled(String provider){
	    		//lblEstado.setText("Provider ON");
	    	}
	    	public void onStatusChanged(String provider, int status, Bundle extras){
	    		Log.i("LocAndroid", "Provider Status: " + status);
	    		//lblEstado.setText("Provider Status: " + status);
	    	}
    	};
    	
    	locationManager.requestLocationUpdates(
    			LocationManager.NETWORK_PROVIDER, 15000, 0, locationListener);
    }*/
    
    
    private class TareaUbicacion extends AsyncTask<String,Integer,String>
	{
		@Override
        protected String doInBackground(String... params) 
		{
            String msg = "";
            
            try 
            {
                registroUbicacion(params[0], params[1], params[2], params[3]);
                
            } 
            catch (Exception ex) 
            {
            	Log.d(TAG, "Error registro en Servidor:" + ex.getMessage());
            }
            
            return msg;
        }

	}
	
	private boolean registroUbicacion(String patente, String latitud, String longitud, String precision)
	{
		boolean reg = false;
		
		final String NAMESPACE = "http://tesis.mobi/";
		final String URL= "http://tesis.mobi/LOADServer/server.php";
		final String METHOD_NAME = "RegistroUbicacion";
		final String SOAP_ACTION = "http://tesis.mobi/RegistroUbicacion";

		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		
		request.addProperty("patente", patente); 
		request.addProperty("lat", latitud); 
		request.addProperty("long", longitud);
		request.addProperty("prec", precision);

		SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
		
		envelope.dotNet = true; 

		envelope.setOutputSoapObject(request);

		HttpTransportSE transporte = new HttpTransportSE(URL);

		try 
		{
			transporte.call(SOAP_ACTION, envelope);

			//SoapPrimitive resultado_xml =(SoapPrimitive)envelope.getResponse();
			Object response = envelope.getResponse();
			String res = response.toString();
			
			if(res.equals("1"))
			{
				Log.d(TAG, " - Registro ubicacion OK.");
				reg = true;
			}
		} 
		catch (Exception e) 
		{
			Log.d(TAG, "Error registro en mi servidor: " + e.getCause() + " || " + e.getMessage());
		} 
		
		return reg;
	}
	
    private void stopService() {
    	locationManager.removeUpdates(this);
    }
	
	@Override
	public void onStart(Intent intent, int startId) {

		
		super.onStart(intent, startId);
		

    	
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (intent != null && intent.getAction() != null
				&& intent.getAction().equals("stop")) {
			// User clicked the notification. Need to stop the service.
			nm.cancel(0);
			stopSelf();
			
		} else {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    		locationManager.requestLocationUpdates(
        			LocationManager.NETWORK_PROVIDER, 20000, 0, this);
			
			Notification notification = new Notification(
					R.drawable.ic_launcher, "Servicio Ubicacion Encendido",
					System.currentTimeMillis());
			notification
					.setLatestEventInfo(this, "Servicio Ubicacion", "Click para apagar",
							PendingIntent.getService(this, 0, new Intent(
									"stop", null, this, this.getClass()), 0));
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			nm.notify(0, notification);
		}
		
		//actualizarPosicion();
		

		
	    
		
		 
	
	}
	
}