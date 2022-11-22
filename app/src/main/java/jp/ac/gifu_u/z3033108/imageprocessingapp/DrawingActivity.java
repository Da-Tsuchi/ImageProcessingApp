package jp.ac.gifu_u.z3033108.imageprocessingapp;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public class DrawingActivity extends AppCompatActivity implements View.OnClickListener {

    private MyView mv;//MyViewクラスのインスタンスを宣言
    private ImageView imgView;
    private Uri uri;
    private Bitmap img,mvBitmap,processImg,bm;
    private Mat mat,matMv,matResult;
    private int height,width;
    private  View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        mv = (MyView) findViewById(R.id.my_view);//MyViewをレイアウトに組み込む
        Intent intent = getIntent();//メインからのintentを受け取る
        imgView = (ImageView) findViewById(R.id.imageView);//取り込んだ画像を表示用;
        uri = intent.getParcelableExtra("imageUri");//uriをprocessingActivityから受け取る
        try {//ファイルを扱うため例外機構を利用
            img = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            mv.img = img;
            height = img.getHeight();
            width = img.getWidth();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //ボタンの準備・設定
        Button btClear = (Button) findViewById(R.id.clear_button);//全消去のボタン設定
        btClear.setOnClickListener(this);
        Button btUndo = (Button)findViewById(R.id.undo_button);//1回戻しのボタン設定
        btUndo.setOnClickListener(this);
        Button btSaved = (Button)findViewById(R.id.saved_button);//描画モードへの切替ボタン設定
        btSaved.setOnClickListener(this);

        //seekbarの設定
        SeekBar seekBar1 = findViewById(R.id.seekbar);//線の太さ用のシークバー
        seekBar1.setProgress(10);//線の太さの最小値と最大値を設定
        seekBar1.setMax(50);
        SeekBar barRed = findViewById(R.id.bar_red);//赤を0～255で調整するためのシークバー
        SeekBar barBlu = findViewById(R.id.bar_blu);//青を0～255で調整するためのシークバー
        SeekBar barGre = findViewById(R.id.bar_gre);//緑を0～255で調整するためのシークバー
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mv.stroke = i;//シークバーの値を線を太さとする

            }

            @Override//今回は使用しない
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override//今回は使用しない
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        barRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mv.red = i;
                mv.color = Color.rgb(mv.red,mv.gre,mv.blu);//シークバーの値を赤の値として色を更新
            }

            @Override//今回は使用しない
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override//今回は使用しない
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        barBlu.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mv.blu = i;
                mv.color = Color.rgb(mv.red,mv.gre,mv.blu);//シークバーの値を青の値として色を更新
            }

            @Override//今回は使用しない
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override//今回は使用しない
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        barGre.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mv.gre = i;
                mv.color = Color.rgb(mv.red,mv.gre,mv.blu);//シークバーの値を緑の値として色を更新
            }

            @Override//今回は使用しない
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override//今回は使用しない
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    @Override
    public void onClick(View view) {

        int id = view.getId();//ボタンのidを取得

        switch(id){
            case R.id.clear_button://クリアが押されたとき
                mv.delete();//配列の内容を全消去
                break;

            case R.id.undo_button://１回戻しが押されたとき
                mv.undo();//undoメソッドを実行
                break;
            case R.id.saved_button:
                // Bitmap化
                mvBitmap = viewToBitmap(mv);
                mv.savedFlag = true;
                mv.img = img;
                mv.mvBitmap = mvBitmap;

                bm = viewToBitmap(mv);
//                    imgView.setImageBitmap(bm);
                mv.savedFlag = false;
                //画像を追加する
                ContentResolver resolver = getApplicationContext().getContentResolver();
                Uri imgCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);//VOLUME_EXTERNAL_PRIMARYにアクセス

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "img"+Calendar.getInstance().getTime()+".jpg");//ファイル名がかぶらないように日付を入れておく
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");// マイムの設定
                values.put(MediaStore.Images.Media.IS_PENDING, 1);// ファイルに排他的にアクセス(IS_PENDINGを１)
                Uri item = resolver.insert(imgCollection, values);//プロバイダにデータを挿入(引数)

                try (OutputStream outstream = resolver.openOutputStream(item)) {//ファイルストリームを利用して、メディアファイルを開く(例外処理が必要なオブジェクトの生成)
                    processImg.compress(Bitmap.CompressFormat.JPEG, 100, outstream);//outputstreamが要求されるため、outputstreamのファイルを準備
                } catch (IOException e) {
                    e.printStackTrace();
                }

                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);//　排他的にアクセスの解除(IS_PENDINGを0)
                resolver.update(item, values, null, null);//メディアファイルをアップデート
        }
    }

    //viewをbitmapに変換する
    public static Bitmap viewToBitmap(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        view.draw(canvas);
        return returnedBitmap;
    }
}
