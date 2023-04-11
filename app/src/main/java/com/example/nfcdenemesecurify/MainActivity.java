package com.example.nfcdenemesecurify;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;


import net.sf.scuba.smartcards.CardFileInputStream;
import net.sf.scuba.smartcards.CardService;

import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.DG11File;
import org.jmrtd.lds.DG1File;
import org.jmrtd.lds.DG2File;
import org.jmrtd.lds.LDS;
import org.jmrtd.lds.MRZInfo;
import org.jmrtd.lds.PACEInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
       if (adapter != null) {
           Intent intent = new Intent(getApplicationContext(), this.getClass());
           intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
           PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
           String[][] filter = new String[][]{new String[]{"android.nfc.tech.IsoDep"}};
           adapter.enableForegroundDispatch(this, pendingIntent, null, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if(adapter != null){
            adapter.disableForegroundDispatch(this);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {

            Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);

            if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {

                /*SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String passportNumber = preferences.getString(KEY_PASSPORT_NUMBER, null);
                String expirationDate = convertDate(preferences.getString(KEY_EXPIRATION_DATE, null));
                String birthDate = convertDate(preferences.getString(KEY_BIRTH_DATE, null));*/

               /* if (passportNumber != null && !passportNumber.isEmpty()
                        && expirationDate != null && !expirationDate.isEmpty()
                        && birthDate != null && !birthDate.isEmpty()) {



                } else {
                    Snackbar.make(passportNumberView, R.string.error_input, Snackbar.LENGTH_SHORT).show();
                }*/
                BACKeySpec bacKey = new BACKey("A40U47500","970103","330127");
                new ReadTask(IsoDep.get(tag), bacKey).execute();
                /*mainLayout.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.VISIBLE);*/
            }
        }
    }
    private class ReadTask extends AsyncTask<Void, Void, Exception> {

        private IsoDep isoDep;
        private BACKeySpec bacKey;

        public ReadTask(IsoDep isoDep, BACKeySpec bacKey) {
            this.isoDep = isoDep;
            this.bacKey = bacKey;
        }
        private DG1File dg1File;

        @Override
        protected Exception doInBackground(Void... params) {


            try {
                Log.e("doInBackground","doInBackground çalıştı");
                if ( isoDep != null){
                    Log.e("isoDep","isoDep null değil ");
                    isoDep.connect(); // ?
                    CardService cardService = CardService.getInstance(isoDep);
                    cardService.open();
                    // JMRTD İLE İLGİLİ KISIM
                    PassportService service = new PassportService(cardService);
                    service.open();
                    boolean paceSucceeded = false;

                    try {

                        CardAccessFile cardAccessFile = new CardAccessFile(service.getInputStream(PassportService.EF_CARD_ACCESS));
                        Collection<PACEInfo> paceInfos = cardAccessFile.getPACEInfos();

                        if (paceInfos != null && paceInfos.size() > 0) {
                            PACEInfo paceInfo = paceInfos.iterator().next();
                            service.doPACE(bacKey, paceInfo.getObjectIdentifier(), PACEInfo.toParameterSpec(paceInfo.getParameterId()));
                            paceSucceeded = true;
                        } else {
                            paceSucceeded = true;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, e);
                        Log.e("try","2.try hata");
                    }
                    service.sendSelectApplet(paceSucceeded);
                    if (!paceSucceeded) {
                        try {
                            service.getInputStream(PassportService.EF_COM).read();
                        } catch (Exception e) {
                            service.doBAC(bacKey);
                        }
                    }
                    LDS lds = new LDS();
                    CardFileInputStream dg1In = service.getInputStream(PassportService.EF_DG1);
                    lds.add(PassportService.EF_DG1, dg1In, dg1In.getLength());
                    dg1File = lds.getDG1File();
                    Log.e("dg1File", String.valueOf(dg1File));

                }else {
                    Log.e("isoDep","ISODEP BOŞ GELDİ");
                }

                /*CardService cardService = CardService.getInstance(isoDep);
                cardService.open();
                // JMRTD İLE İLGİLİ KISIM
                PassportService service = new PassportService(cardService);
                service.open();
                boolean paceSucceeded = false;*/





            } catch (Exception e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            /*mainLayout.setVisibility(View.VISIBLE);
            loadingLayout.setVisibility(View.GONE);*/

            if (result == null) {

                MRZInfo mrzInfo = dg1File.getMRZInfo();

                Log.e("Alınan MRZ",mrzInfo.getPrimaryIdentifier());

            } else {
                //Snackbar.make(passportNumberView, exceptionStack(result), Snackbar.LENGTH_LONG).show();
            }
        }
    }
}