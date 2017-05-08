package name.caiyao.crackwhatsapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread() {
            @Override
            public void run() {
                crackWhatsApp();
            }
        }.start();
    }

    public ArrayList<String> runTool( ArrayList<String> commands) {
        ArrayList<String> output = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("su");
            BufferedOutputStream shellInput = new BufferedOutputStream(
                    process.getOutputStream());
            BufferedReader shellOutput = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            for (String command : commands) {
                Log.i("TAG", "command: " + command);
                shellInput.write((command + " 2>&1\n").getBytes());
            }
            shellInput.write("exit\n".getBytes());
            shellInput.flush();
            String line;
            while ((line = shellOutput.readLine()) != null) {
                Log.i("TAG", "command output: " + line);
                output.add(line);
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }

    private void crackWhatsApp() {
        String STORE_PATH_PHONE = "/data/data/name.caiyao.crackwhatsapp/files";
        String dbPath = "/data/data/com.whatsapp/databases/msgstore.db";
        String fileName = new File(dbPath).getName();
        ArrayList<String> commands = new ArrayList<>();
        commands.add("chmod 777 " + dbPath);
        commands.add("cat " + dbPath + " > " + STORE_PATH_PHONE + "/" + fileName);
        commands.add("chmod 777 " + STORE_PATH_PHONE + "/" + fileName);
        runTool(commands);
        commands.clear();
        Log.i("TAG", "开始读取数据库");
        SQLiteDatabase database = SQLiteDatabase.openDatabase(STORE_PATH_PHONE + "/" + fileName, null, 0);
        Cursor cursor = null;
        try {
            cursor = database.query("messages", null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String s = cursor.getString(cursor.getColumnIndex("key_remote_jid"));
                Log.i("TAG", s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            database.close();
        }
        Log.i("TAG", "complete");
    }
}
