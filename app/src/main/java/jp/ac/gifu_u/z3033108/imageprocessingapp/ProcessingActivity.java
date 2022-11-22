package jp.ac.gifu_u.z3033108.imageprocessingapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class ProcessingActivity extends AppCompatActivity {
    private ImageView imgView,imgProcessView;//取り込んだ画像、処理後の画像の表示用
    private Bitmap img,img2;
    private Bitmap processImg = null;//読み込んだ画像用(メインから受け取る),処理後の画像のbitmap用
    private Uri uri,uri_draw;
    private int width;//画像の幅用
    private int height;//画像の高さ用
    private int[] pixels;
    private Mat mat,mat1,mat2,matOri;//opencvを利用するときの型
    private EditText etn_kernel,etn_k;//カーネルサイズ、重みの値を画面で入力する用に準備
    private Scalar color = new Scalar(255,255,255);

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        //opencvが呼び出されたときにlogで表示
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    mat = new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    //以下のコードはopencvのエラーを解決するために　https://stackoverflow.com/questions/35090838/no-implementation-found-for-long-org-opencv-core-mat-n-mat-error-using-opencv　より引用
    //AndroidがOpenCVライブラリをロードする前にonCreateメソッドを呼び出すため、OpenCVManagerを使ってOpenCVの非同期初期化を行う。onCreateメソッドの前に、BaseLoaderCallbackを作成した。
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    //ここまで

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        //カーネルサイズ、kの入力欄を設定
        etn_kernel = findViewById(R.id.kernel_edit);
        etn_k = findViewById(R.id.k_edit);

        Intent intent = getIntent();//メインからのintentを受け取る
        imgView = (ImageView) findViewById(R.id.img2_view);//取り込んだ画像を表示用;
        imgProcessView = (ImageView) findViewById(R.id.process_view);//処理後の画像を表示用
        uri = intent.getParcelableExtra("imageUri");//uriをmainから受け取る
        try {//ファイルを扱うため例外機構を利用
            img = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            imgView.setImageBitmap(img);
            width = img.getWidth();
            height = img.getHeight();
            pixels = new int[width * height];
            img.getPixels(pixels, 0, width, 0, 0, width, height);

        } catch (IOException e) {
            e.printStackTrace();
        }

//        ここから各種ボタンの設定
        //描画モードへ遷移するボタンの設定
        Button btnDrawing = (Button) findViewById(R.id.drawing_btn);//画面遷移するボタン処理
        btnDrawing.setOnClickListener(v -> {//描画クラスに移動
            Intent intent_draw = new Intent(getApplication(), DrawingActivity.class);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();//処理した画像をDrawingActivityに表示させるため
            if(processImg==null){//nullのときは何もしない

            }else {
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), processImg, "img"+ Calendar.getInstance().getTime(), null);//bitmap型からuri型へ変換
                uri_draw = Uri.parse(path);
                //bitmapで送るとエラーが発生するおそれがあるためuri型で送る(送った先でbitmapに変換)
                intent_draw.putExtra("imageUri", uri_draw);//読み込んだbitmapデータを処理クラスに渡す
                startActivity(intent_draw);
            }
        });

        Button btnBack = findViewById(R.id.back_btn);//画像選択の画面に戻る処理
        btnBack.setOnClickListener(v -> {
            Intent intent_back = new Intent(getApplication(), MainActivity.class);
            startActivity(intent_back);
        });

        Button btnGray = findViewById(R.id.gray_btn);//白黒化するためのボタンを設定
        btnGray.setOnClickListener(v -> {//押したときの処理
            try {
                gray();
            } catch (Exception e) {
            }
        });
        Button btnGaussian = findViewById(R.id.gaussian_btn);//ガウシアンフィルタをかけるためのボタンを設定
        btnGaussian.setOnClickListener(v -> {//押したときの処理
            try {
                gaussian();
            } catch (Exception e) {

            }
        });

        //鮮鋭化（アンシャープマスク）の設定
        Button btnUnsharpmask = findViewById(R.id.unsharp_btn);//アンシャープマスクをかけるためのボタンを設定
        btnUnsharpmask.setOnClickListener(v -> {//押したときの処理
            try {
                unSharp();
            } catch (Exception e) {

            }
        });
        Button btnBinary = findViewById(R.id.binary_btn);//アンシャープマスクをかけるためのボタンを設定
        btnBinary.setOnClickListener(v -> {//押したときの処理
            try {
                binary();
            } catch (Exception e) {

            }
        });

        Button btnSaved = findViewById(R.id.saved_btn);
        btnSaved.setOnClickListener(v -> {
            if(processImg==null){//nullのときは何もしない
            }else {
                imgSaved();
            }
        });

        Button btnContinue = findViewById(R.id.continue_btn);//継続して処理をするためのボタンを設定
        btnContinue.setOnClickListener(v -> {//押したときの処理
            try {
                if(processImg==null){//nullのときは何もしない

                }else {
                    img = processImg;
                    imgView.setImageBitmap(img);
                }
            } catch (Exception e) {

            }
        });

        //輪郭抽出するための関数
        Button btnEdge = findViewById(R.id.edge_btn);
        btnEdge.setOnClickListener(v -> {
            try {
                edge();
            } catch (Exception e) {

            }
        });
    }


    //読み込んだ画像を白黒画像に変換するメソッド
    public void gray(){
        matOri = new Mat();//原画像のbitmapをmat型に変換
        img2 = img;//処理用画像を表示させるために内容をコピーする
        Utils.bitmapToMat(img2, matOri);//bitmapをmat形式に変換
        Imgproc.cvtColor(matOri, matOri, Imgproc.COLOR_RGB2GRAY);//カラーから白黒に変換
        processImg = img2.copy(Bitmap.Config.ARGB_8888, true);//処理用画像を表示させるために内容をコピーする
        Utils.matToBitmap(matOri, processImg);//mat型からbitmapに変換
        imgProcessView.setImageBitmap(processImg);//処理後の画像を表示
    }

    public void gaussian(){
        mat2 = new Mat();
        img2 = img;
        int ksize = Integer.parseInt(etn_kernel.getText().toString());//入力されたカーネルサイズを保存
        Size size = new Size(ksize, ksize);
        Utils.bitmapToMat(img2, mat2);//bitmapをmat形式に変換
        Imgproc.GaussianBlur(
                mat2, mat2, size, 0, 0); // (2)
        processImg = img2.copy(Bitmap.Config.ARGB_8888, true);
        Utils.matToBitmap(mat2, processImg);
        imgProcessView.setImageBitmap(processImg);
    }

    public void unSharp(){
        matOri = new Mat();
        Mat matMiddle = new Mat();
        img2 = img;
        int size = Integer.parseInt(etn_kernel.getText().toString());//入力されたカーネルサイズを保存
        int k = Integer.parseInt(etn_k.getText().toString());//入力されたｋの値を保存(強調係数)
        Utils.bitmapToMat(img2, matOri);//bitmapをmat形式に変換

        Size ksize = new Size(size, size);
        Imgproc.blur(matOri, matMiddle, ksize);//平滑化フィルタ(size*size)
        // sharpened = original + (original − blurred) × k.
        //           = (1 + k) * original - k * blurred
        //アンシャープマスクの上記式を表現
        Core.addWeighted(matOri, k + 1, matMiddle, -k, 0, matOri);
        //src1 - 1 番目の入力配列．
        //alpha - 1 番目の配列要素に対する重み付け．
        //src2 - src1 と同じサイズ，同じチャンネル番号の2番目の入力配列．
        //beta - 2 番目の配列の要素に対する重み．
        //gamma - それぞれの和に加えられるスカラー．
        //dst - 入力配列と同じサイズ，同じチャンネル数の出力配列．

        processImg = img2.copy(Bitmap.Config.ARGB_8888, true);
        Utils.matToBitmap(matOri, processImg);
        imgProcessView.setImageBitmap(processImg);
    }

    public void binary(){
        matOri = new Mat(height,width,CvType.CV_8UC1);//原画像のbitmapをmat型に変換
        Utils.bitmapToMat(img,matOri);
        Imgproc.cvtColor(matOri, matOri, Imgproc.COLOR_RGB2GRAY);//カラーから白黒に変換
        Imgproc.threshold(matOri, matOri, 0.0, 255.0, Imgproc.THRESH_OTSU);//大津の方法で画像を二値化
        processImg = img.copy(Bitmap.Config.ARGB_8888, true);
        Utils.matToBitmap(matOri, processImg);
        imgProcessView.setImageBitmap(processImg);
    }

    public void edge(){
        matOri = new Mat(height,width,CvType.CV_8UC3);//画像を格納するためのmat型を用意
        mat1 = new Mat(height,width,CvType.CV_8UC3);//画像を格納するためのmat型を用意(途中経過で必要なので準備)
        mat2 = mat1;
        Utils.bitmapToMat(img,matOri);
        Utils.bitmapToMat(img,mat1);//imgのコピー
        Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_RGB2GRAY);//白黒画像に変換
        Imgproc.GaussianBlur(mat1, mat1, new Size(51, 51), 0, 0);//ガウシアンフィルタでぼかす
        Imgproc.threshold(mat1, mat1, 0.0, 255.0, Imgproc.THRESH_OTSU);//大津の方法で画像を二値化
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();//MatOfPoint型はN行1列の行列で、要素一つにdoubleの配列(x,yに相当)が格納されている
        Mat hierarchy = new Mat(mat1.height(),mat1.width(), CvType.CV_8UC3);//輪郭の数を格納する変数

        Imgproc.findContours(mat1, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);//輪郭を検出し、格納する
        //contours - 検出された輪郭．各輪郭は，点のベクトルとして格納される
        //hierarchy - オプション出力ベクトル．画像のトポロジーに関する情報が格納されている．輪郭の数と同じ数の要素を持つ。
        //各i番目の輪郭 contours[i] に対して，要素 hierarchy[i][0] , hierarchy[i][1] , hierarchy[i][2] , hierarchy[i][3] は，それぞれ同じ階層レベルにある次の輪郭と前の輪郭，最初の子の輪郭，親の輪郭における0基準のインデックスに設定されています．輪郭 i に対して，次の輪郭，前の輪郭，親の輪郭，入れ子の輪郭が存在しない場合， hierarchy[i] の対応する要素は負の値になる．
        //RETA_EXTERNAL - 一番外側の輪郭のみ抽出
        //CHAIN_APPROX_SIMPLE - 端点のみ

        Imgproc.drawContours(matOri,contours,-1,color,5);//原画像に輪郭を加える
        //image - 出力される画像．
        //contours - すべての入力輪郭．各輪郭は，点ベクトルとして格納される
        //contourIdx - 描画する輪郭を指定するパラメータ．-1のときすべての輪郭が描画
        //color - 輪郭の色
        //thickness - 輪郭が描かれる線の太さ．これが負値の場合（例えば， thickness=#FILLED ），輪郭の内側が描画される
        processImg = img.copy(Bitmap.Config.ARGB_8888, true);
        Utils.matToBitmap(matOri,processImg);
        imgProcessView.setImageBitmap(processImg);
    }

    public void imgSaved(){
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