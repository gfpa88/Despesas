package pt.gon.expensivessheet.ws;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gon.expensivessheet.GoogleCrendentialSingleton;
import pt.gon.expensivessheet.R;
import pt.gon.expensivessheet.ws.model.Configuration;
import pt.gon.expensivessheet.ws.model.Movimento;

public class ApiService {

    private static ApiService instance;

    private ApiService() {
        // Private constructor to prevent instantiation
    }

    public synchronized static ApiService getInstance() {
        if(instance == null) {
            synchronized (ApiService.class ){
                if (instance == null){
                    instance = new ApiService();
                }
            }
        }
        return instance;
    }
    @NonNull
    private Sheets getService(Context context) {

        return new Sheets.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                .setApplicationName(context.getString(R.string.app_name))
                .build();
    }

    @NonNull
    public Configuration getConfiguration(String fileId, Context context) throws Exception {
        Sheets service = getService(context);

        List<List<Object>> versionTab = service.spreadsheets().values().get(fileId, "Version!A1:A3").execute().getValues();
        Configuration config = new Configuration();
        config.setName(versionTab.get(0).get(0).toString());
        config.setVersion(versionTab.get(1).get(0).toString());
        config.setLanguage(versionTab.get(2).get(0).toString());

        return config;
    }



    public List<String> getPersonOrCategory(String fileId, String tab, Context context) throws IOException {
        Sheets service = getService(context);

        List<String> mlist = new ArrayList<>();
        List<List<Object>> drive = service.spreadsheets().values().get(fileId, tab).execute().getValues();
        for (List<Object> c : drive) {
            for (Object t : c) {
                mlist.add(t.toString());
            }
        }
        return mlist;
    }

    public void addRow(String fileId, String range, List<List<Object>> values, Context context) throws Exception {

        Sheets service = getService(context);

        ValueRange insert = new ValueRange();
        insert.setValues(values);
        insert.setRange(range);
        service.spreadsheets().values().append(fileId, range, insert).setValueInputOption("RAW").setInsertDataOption("INSERT_ROWS").execute();
    }
    public void updateRow(String fileId, String range, List<List<Object>> values, Context context) throws Exception{
        Sheets service = getService(context);

        ValueRange insert = new ValueRange();
        insert.setValues(values);
        insert.setRange(range);


        service.spreadsheets().values().update(fileId, range, insert).setValueInputOption("RAW").execute();
    }
    public boolean deleteRow(String fileId, String tab, Context context, Integer StartIndex, Integer EndIndex) {
        Spreadsheet spreadsheet = null;

        Sheets service = getService(context);
        try {
            spreadsheet = service.spreadsheets().get(fileId).execute();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
        Request request = new Request();
        DeleteDimensionRequest deleteDimensionRequest = new DeleteDimensionRequest();
        DimensionRange dimensionRange = new DimensionRange();
        dimensionRange.setDimension("ROWS");
        dimensionRange.setStartIndex(StartIndex);
        dimensionRange.setEndIndex(EndIndex);

        Sheet sh = null;
        for (Sheet s : spreadsheet.getSheets()) {
            if (s.getProperties().getTitle().equals(tab)) {
                sh = s;
                break;
            }
        }
        dimensionRange.setSheetId(sh.getProperties().getSheetId());
        deleteDimensionRequest.setRange(dimensionRange);

        request.setDeleteDimension(deleteDimensionRequest);

        List<Request> requests = new ArrayList<Request>();
        requests.add(request);
        content.setRequests(requests);

        try {
            service.spreadsheets().batchUpdate(fileId, content).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
        }
        return true;
    }

    public List<File> getFiles( Context context) throws IOException {

        Drive driveService = new Drive.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                GoogleCrendentialSingleton.getInstance().getmGoogleAccountCredential())
                .setApplicationName(context.getString(R.string.app_name))
                .build();

        Drive.Files.List request = driveService.files().list()
                //.setPageSize(100)
                // Available Query parameters here:
                //https://developers.google.com/drive/v3/web/search-parameters
                .setQ("mimeType = 'application/vnd.google-apps.spreadsheet' and (name contains 'Despesa' or name contains 'Expense') and trashed = false")
                .setFields("nextPageToken, files(id, name)");

        FileList result = request.execute();

        return result.getFiles();
    }

    public List<Movimento> getTransactions(String fileId, String tab, Context context) throws IOException {

        Sheets service = getService(context);
        List<List<Object>> movimentos;
        List<Movimento> movimentoList = new ArrayList<>();
        movimentos = service.spreadsheets().values().get(fileId, tab).execute().getValues();

        for (List<Object> movimento : movimentos) {
            Movimento m = new Movimento();
            m.setData(movimento.get(0).toString());
            m.setDescricao(movimento.get(1).toString());
            m.setValor(movimento.get(2).toString());
            m.setTipo(movimento.get(3).toString());
            m.setPessoa(movimento.get(4).toString());
            m.setMes(movimento.get(5).toString());
            m.setAno(movimento.get(6).toString());

            movimentoList.add(m);
        }
        movimentoList.remove(0);
        return movimentoList;
    }
}