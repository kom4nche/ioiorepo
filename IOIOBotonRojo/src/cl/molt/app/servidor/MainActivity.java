package cl.molt.app.servidor;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private Button btnIniciar;
	private Button btnDetener;
	PowerManager pm;
	PowerManager.WakeLock wl;
	//final Context context;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		final Context context = getApplicationContext();
		
		//startService(new Intent(this, ServicioAlertas.class));
		//startService(new Intent(this, ServicioUbicacion.class));
		
		btnIniciar = (Button)findViewById(R.id.button1);
        btnDetener = (Button)findViewById(R.id.button2);
        
		 pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
         
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         wl.acquire();
         

         
        
        btnIniciar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//comenzarLocalizacion();
				
				//startService(new Intent(context, ServicioUbicacion.class));
				startService(new Intent(context, ServicioAlertas.class));
			}
		});
        
        btnDetener.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		    	//locManager.removeUpdates(locListener);
				
				stopService(new Intent(context, ServicioAlertas.class));
			}
		});
	}
	
	
    @Override
    public void onDestroy() {
        super.onDestroy();
        wl.release();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}