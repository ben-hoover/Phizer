package io.phizer.phizer;

        import android.content.Intent;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import java.io.File;
//Get path from intent
public class AddNewFolder extends AppCompatActivity {
    String message;
    String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_folder);
        message = "";
        Intent original = getIntent();
        //Make sure "add folder" button sends the path with the intent with EXTRA_PATH as the key
        path = original.getStringExtra("path");
        Button butt = (Button) findViewById(R.id.button4);
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddNewFolder.this,MainActivity.class);
                i.putExtra("path",path);
                startActivity(i);
            }
        });
    }
    public void onSetName(View view){
        EditText editText = (EditText) findViewById(R.id.editText);
        message = editText.getText().toString();
        if(!message.isEmpty()) {
            TextView textView = (TextView) findViewById(R.id.textView3);
            textView.setText(message);
        }
    }
    public void onExit(View view){
        File dir= new File(path+"/"+message);
        dir.mkdir();
        //Change FolderTest to path defined activity
        Intent intent = new Intent(AddNewFolder.this, MainActivity.class);
        //intent.putExtra("FolderName",message);
        intent.putExtra("path",path);
        startActivity(intent);
    }
}