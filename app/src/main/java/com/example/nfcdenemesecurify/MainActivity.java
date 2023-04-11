package com.example.nfcdenemesecurify;


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

import androidx.appcompat.app.AppCompatActivity;

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


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    private View mainLayout;
    private View loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      /*  SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        passportNumberView = findViewById(R.id.input_passport_number);
        expirationDateView = findViewById(R.id.input_expiration_date);
        birthDateView = findViewById(R.id.input_date_of_birth);*/

        /*mainLayout = findViewById(R.id.main_layout);
        loadingLayout = findViewById(R.id.loading_layout);*/

        /*passportNumberView.setText(preferences.getString(KEY_PASSPORT_NUMBER, null));
        expirationDateView.setText(preferences.getString(KEY_EXPIRATION_DATE, null));
        birthDateView.setText(preferences.getString(KEY_BIRTH_DATE, null));*/

       /* passportNumberView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                        .edit().putString(KEY_PASSPORT_NUMBER, s.toString()).apply();
            }
        });*/

       /* expirationDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = loadDate(expirationDateView);
                DatePickerDialog dialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        saveDate(expirationDateView, year, monthOfYear, dayOfMonth, KEY_EXPIRATION_DATE);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                getFragmentManager().beginTransaction().add(dialog, null).commit();
            }
        });

        birthDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = loadDate(birthDateView);
                DatePickerDialog dialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        saveDate(birthDateView, year, monthOfYear, dayOfMonth, KEY_BIRTH_DATE);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                getFragmentManager().beginTransaction().add(dialog, null).commit();
            }
        });*/
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
        if (adapter != null) {
            adapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.e("OnNewIntetn", "çalıştı");

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {

            Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);
            Log.e("TAG", String.valueOf(tag));
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
                Log.e("Backey", String.valueOf(bacKey));
                new ReadTask(IsoDep.get(tag), bacKey).execute();
                /*mainLayout.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.VISIBLE);*/
            }
        }
    }

    /* private static String exceptionStack(Throwable exception) {
         StringBuilder s = new StringBuilder();
         String exceptionMsg = exception.getMessage();
         if (exceptionMsg != null) {
             s.append(exceptionMsg);
             s.append(" - ");
         }
         s.append(exception.getClass().getSimpleName());
         StackTraceElement[] stack = exception.getStackTrace();

         if (stack.length > 0) {
             int count = 3;
             boolean first = true;
             boolean skip = false;
             String file = "";
             s.append(" (");
             for (StackTraceElement element : stack) {
                 if (count > 0 && element.getClassName().startsWith("com.tananaev")) {
                     if (!first) {
                         s.append(" < ");
                     } else {
                         first = false;
                     }

                     if (skip) {
                         s.append("... < ");
                         skip = false;
                     }

                     if (file.equals(element.getFileName())) {
                         s.append("*");
                     } else {
                         file = element.getFileName();
                         s.append(file.substring(0, file.length() - 5)); // remove ".java"
                         count -= 1;
                     }
                     s.append(":").append(element.getLineNumber());
                 } else {
                     skip = true;
                 }
             }
             if (skip) {
                 if (!first) {
                     s.append(" < ");
                 }
                 s.append("...");
             }
             s.append(")");
         }
         return s.toString();
     }*/
    ///NFC ile okuma bilgilerin servisten alınması
    private class ReadTask extends AsyncTask<Void, Void, Exception> {

        private IsoDep isoDep;
        private BACKeySpec bacKey;

        public ReadTask(IsoDep isoDep, BACKeySpec bacKey) {
            this.isoDep = isoDep;
            this.bacKey = bacKey;
        }

        private DG1File dg1File;
        private DG2File dg2File; //MRZ içerisindeki kisisel bilgiler alınır . Ad soyad gibi
        private DG11File dg11File; // Adres ve aytıntılı bilgiler
        private Bitmap bitmap;

        @Override
        protected Exception doInBackground(Void... params) {
            try {

                Log.e("doInBackground","doInBackground ÇALIŞTI");
                isoDep.connect();

                CardService cardService = CardService.getInstance(isoDep);
                cardService.open();

                PassportService service = new PassportService(cardService);
                service.open();


                boolean paceSucceeded = false; // pace başarılı oldu. Test ortamıdır

                try {
                    Log.e("try","Try'a girdi");
                    CardAccessFile cardAccessFile = new CardAccessFile(service.getInputStream(PassportService.EF_CARD_ACCESS));
                   // CardFileInputStream cardFileInputStream = new CardFileInputStream(service.getInputStream(PassportService.EF_CARD_ACCESS));
                    Collection<PACEInfo> paceInfos = cardAccessFile.getPACEInfos();//hız bilgileri
                    if (paceInfos != null && paceInfos.size() > 0) {
                        PACEInfo paceInfo = paceInfos.iterator().next();
                        service.doPACE(bacKey, paceInfo.getObjectIdentifier(), PACEInfo.toParameterSpec(paceInfo.getParameterId()));
                        paceSucceeded = true;
                    } else {
                        Log.e("paceInfo","BOŞŞ");
                        paceSucceeded = true;
                    }
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
                Log.e("1","1");
                service.sendSelectApplet(paceSucceeded);
                Log.e("2","2");
                if (!paceSucceeded) {
                    try {
                        Log.e("3","3");
                       service.getInputStream(PassportService.EF_COM); // hata burada
                        Log.e("4","4");
                    } catch (Exception e) {
                        Log.e("5","5");
                        Log.e("G", String.valueOf(bacKey));
                        service.doBAC(bacKey);  // Patladığımız yer
                        Log.e("ç", String.valueOf(bacKey));
                        Log.e("6","6");
                    }
                }

                Log.e("7","7");
                LDS lds = new LDS();
                CardFileInputStream dg1In = service.getInputStream(PassportService.EF_DG1);
                lds.add(PassportService.EF_DG1, dg1In, dg1In.getLength());
                dg1File = lds.getDG1File();
                Log.e("dg1File", String.valueOf(dg1File));

            } catch (Exception e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {

            if (result == null) {

                MRZInfo mrzInfo = dg1File.getMRZInfo();

                Log.e("Alınan MRZ",mrzInfo.getPrimaryIdentifier());

            } else {
                //Snackbar.make(passportNumberView, exceptionStack(result), Snackbar.LENGTH_LONG).show();
            }
        }

    }

}