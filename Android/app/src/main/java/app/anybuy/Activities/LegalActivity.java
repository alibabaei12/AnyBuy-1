package app.anybuy.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import app.anybuy.R;

public class LegalActivity extends AppCompatActivity {

    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int FP = ViewGroup.LayoutParams.FILL_PARENT;

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK)
            return true;//不执行父类点击事件
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);

        TableLayout tableLayout = (TableLayout)findViewById(R.id.legalPageTable);
        tableLayout.setStretchAllColumns(true);

        TableRow tableRow = new TableRow(this);
        TextView tv = new TextView(this);
        tv.setText("I am a serious legal page. ୧(๑•̀⌄•́๑)૭✧");
        tableRow.addView(tv);
        tableLayout.addView(tableRow, new TableLayout.LayoutParams(FP, WC));
    }
}
