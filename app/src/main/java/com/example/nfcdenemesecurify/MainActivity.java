package com.example.nfcdenemesecurify;


import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
import org.jmrtd.lds.FaceImageInfo;
import org.jmrtd.lds.FaceInfo;
import org.jmrtd.lds.LDS;
import org.jmrtd.lds.MRZInfo;
import org.jmrtd.lds.PACEInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    ImageView imageView;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

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
        imageView = findViewById(R.id.photo);
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
            IsoDep nfc = IsoDep.get(tag);
            nfc.setTimeout(10000);
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
                new ReadTask(nfc, bacKey).execute();
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
        public Bitmap bitmap;

        @Override
        protected Exception doInBackground(Void... params) {
            try {

                Log.e("doInBackground","doInBackground ÇALIŞTI");

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
                service.doBAC(bacKey);  // Patladığımız yer
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
                ///////////// DG1FİLE
                CardFileInputStream dg1In = service.getInputStream(PassportService.EF_DG1);
                lds.add(PassportService.EF_DG1, dg1In, dg1In.getLength());
                dg1File = lds.getDG1File();

                Log.e("dg1File", String.valueOf(dg1File));

                ///////////// DG11FİLE
                CardFileInputStream dg11In = service.getInputStream(PassportService.EF_DG11);
                lds.add(PassportService.EF_DG11, dg11In, dg11In.getLength());
                dg11File = lds.getDG11File();
                ///////////// DG2FİLE
                CardFileInputStream dg2In = service.getInputStream(PassportService.EF_DG2);
                lds.add(PassportService.EF_DG2, dg2In, dg2In.getLength());
                dg2File = lds.getDG2File();
                Log.e("dg2File", String.valueOf(dg2File));

                bitmap = FaceInfoChangem(dg2File,bitmap);
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
                Log.e("DG11","////////////");
                Log.e("Adress", String.valueOf(dg11File.getPermanentAddress()));
                Log.e("Other Name", String.valueOf(dg11File.getOtherNames()));
                Log.e("Personal Number",dg11File.getPersonalNumber());
                Log.e("Place of Birth", String.valueOf(dg11File.getPlaceOfBirth()));
                Log.e("Date of Birth (in full)", String.valueOf(dg11File.getFullDateOfBirth()));
                //Log.e("Telephone Number(s)",dg11File.getTelephone());  //değerlere ulaşılmıyor
               // Log.e("Profession",dg11File.getProfession());  //değerlere ulaşılmıyor
                //Log.e("Title",dg11File.getTitle());   //değerlere ulaşılmıyor
                //Log.e("Personal Summary",dg11File.getPersonalSummary()); //değerlere ulaşılmıyor
                Log.e("Proof of Citizenship ", String.valueOf(dg11File.getProofOfCitizenship()));
                Log.e("Number of OtherValid ", String.valueOf(dg11File.getOtherValidTDNumbers()));
                Log.e("TAG", String.valueOf(dg11File.getTag()));
               // Log.e("Custody Information",dg11File.getCustodyInformation()); // değerlere ulaşılmıyor

                /// IMAGE
                imageView.setImageBitmap(bitmap);

            } else {
                //Snackbar.make(passportNumberView, exceptionStack(result), Snackbar.LENGTH_LONG).show();
            }
        }



    }
    public Bitmap  FaceInfoChangem(DG2File dg2File,Bitmap bitmapm){
    Log.e("ad","adaw");

        List<FaceImageInfo> allFaceImageInfos = new ArrayList<>();
        List<FaceInfo> faceInfos = dg2File.getFaceInfos();
        for (FaceInfo faceInfo : faceInfos) {
            allFaceImageInfos.addAll(faceInfo.getFaceImageInfos());
        }
        if (!allFaceImageInfos.isEmpty()) {
            FaceImageInfo faceImageInfo = allFaceImageInfos.iterator().next();

            int imageLength = faceImageInfo.getImageLength();
            DataInputStream dataInputStream = new DataInputStream(faceImageInfo.getImageInputStream());
            byte[] buffer = new byte[imageLength];
            try {
                dataInputStream.readFully(buffer, 0, imageLength);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            InputStream inputStream = new ByteArrayInputStream(buffer, 0, imageLength);

            bitmapm = BitmapFactory.decodeStream(inputStream);
            /*bitmap = ImageUtil.decodeImage(
                    MainActivity.this, faceImageInfo.getMimeType(), inputStream);*/

        }
        return bitmapm;
    }
}