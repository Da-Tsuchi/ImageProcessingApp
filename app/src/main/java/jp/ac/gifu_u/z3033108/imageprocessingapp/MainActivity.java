package jp.ac.gifu_u.z3033108.imageprocessingapp;


import static android.nfc.NfcAdapter.EXTRA_DATA;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_GALLERY = 0;//初期値0を入れておく
    private ImageView imgView;//読み込んだ画像の表示用
    private Uri resultUri;//
    private InputStream inputStream;//
    private Bitmap img;//ビットマップ型に変換する変数を用意

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = (ImageView) findViewById(R.id.img_view);//取り込んだ画像を表示用
        Button imgBut = findViewById(R.id.img_button);//写真を選択するためのボタンを設定
        imgBut.setOnClickListener( v -> {//押したときの処理
            // ギャラリー呼び出し
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);//別の写真を選択した際にその都度更新する
            intent.setType("image/*");//写真のフォルダにアクセス
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setAction(Intent.ACTION_GET_CONTENT);//ファイルのコピーを取得してアプリにインポートする
            startActivityForResult(intent, REQUEST_GALLERY);//開いたアクティビティから何かしらの情報を受け取ることを可能とする
        });

        Button btnProcessing = (Button) findViewById(R.id.processing_btn);//画面遷移するボタン処理
        btnProcessing.setOnClickListener(v-> {//処理クラスに移動
            if(resultUri==null){
                //画像を読み込んでいないときのヌルポインタ対策
            }else {
                Intent intent = new Intent(getApplication(), ProcessingActivity.class);
                //bitmapで送るとエラーが発生するおそれがあるためuri型で送る(送った先でbitmapに変換)
                intent.putExtra("imageUri", resultUri);//読み込んだbitmapデータを処理クラスに渡す
                startActivity(intent);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //requestCode->メソッドが呼び出された時に、どのアクティビティから戻ってきたのかを判別
        //resultCode->元のアクティビティに戻ってくるまえにどのような処理が行われたのかを整数で判別(写真を選択でRESULT_OK)
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            try {//ファイルにアクセスするため例外処理を実行
                //バイナリファイルとしてよみこみ
                resultUri = data.getData();//選択した写真のデータをuri型で取得
                inputStream = getContentResolver().openInputStream(resultUri);//uriー＞InputStreamに変換
                img = BitmapFactory.decodeStream(inputStream);//InputStream->Bitmapに変換
                // 選択した画像を表示
                imgView.setImageBitmap(img);
                inputStream.close();
            } catch (Exception e) {

            }
        }
    }
}