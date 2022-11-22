package jp.ac.gifu_u.z3033108.imageprocessingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class MyView extends View {

    //フィールド変数
    // イベント発生時の X 座標、Y 座標を保存するための動的配列
    private ArrayList<Integer> array_x;
    private ArrayList<Integer> array_y;
    private ArrayList<Boolean> array_status;
    private ArrayList<Integer> array_color;//色を保存するための動的配列
    private ArrayList<Integer> array_stroke;//線の太さを保存するための動的配列
    private ArrayList<Integer> array_r;
    private ArrayList<Integer> array_g;
    private ArrayList<Integer> array_b;
    boolean savedFlag = false;
    private Rect src, dst;//画像の範囲指定用
    private int x_min, x_max, y_min, y_max;//4点の座標用

    Bitmap img, mvBitmap;

    //RGBの値を保存して置くための変数
    int red;
    int blu;
    int gre;

    int color = Color.rgb(red, blu, gre);//色の値(初期値は黒)
    int stroke = 20;//線の太さ（初期値は20）


    public MyView(Context context) {
        super(context);
        //各動的配列を初期化する
        array_x = new ArrayList<>();
        array_y = new ArrayList<>();
        array_status = new ArrayList<Boolean>();
        array_color = new ArrayList<Integer>();
        array_r = new ArrayList<Integer>();
        array_g = new ArrayList<Integer>();
        array_b = new ArrayList<Integer>();

        array_stroke = new ArrayList<Integer>();
        array_color.add(0, color);
        array_stroke.add(0, stroke);
        img = null;
    }

    //アプリ起動のためのコンストラクタ(2引数)
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //各動的配列を初期化する
        array_x = new ArrayList<Integer>();
        array_y = new ArrayList<>();
        array_status = new ArrayList<Boolean>();
        array_color = new ArrayList<Integer>();
        array_stroke = new ArrayList<Integer>();
        array_r = new ArrayList<Integer>();
        array_g = new ArrayList<Integer>();
        array_b = new ArrayList<Integer>();
        array_color.add(0, color);//色の初期値は赤
        array_stroke.add(0, stroke);
        // 以下省略
        img = null;
    }

    // ビューの描画を行うときに呼ばれるメソッド
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 背景を白色で塗りつぶす
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setARGB(0, 255, 255, 255);
        canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), p);
        // 描画用の Paint オブジェクトを用意
        p = new Paint();
        if (img != null) {
            // 元画像から切り出す位置を指定
            src = new Rect(0, 0, img.getWidth(), img.getHeight());
            //リサイズの設定
            if (img.getWidth() < img.getHeight()) {
                x_min = 0;
                y_min = 0;
                x_max = canvas.getHeight() * img.getWidth() / img.getHeight();
                y_max = canvas.getHeight();
            } else {
                x_min = 0;
                y_min = 0;
                x_max = canvas.getWidth();
                y_max = canvas.getWidth() * img.getHeight() / img.getWidth();
            }
            dst = new Rect(x_min, y_min, x_max, y_max);
            canvas.drawBitmap(img, src, dst, null);
        }
        p.setStyle(Paint.Style.STROKE);
        p.setARGB(255, 0, 0, 0);


        // 配列内の座標を読み出して線（軌跡）を描画
        for (int i = 1; i < array_status.size(); i++) {
            // 描画するように(true)状態値が与えられているとき
            // 一度離してしてから次に押されるまでの移動分は描画しない
            if (array_status.get(i)) {
                // 開始点の終了点の座標の値を取得
                int x1 = array_x.get(i - 1);
                int x2 = array_x.get(i);
                int y1 = array_y.get(i - 1);
                int y2 = array_y.get(i);

                p.setARGB(255, array_r.get(i), array_g.get(i), array_b.get(i));//各地点での色を取得

                p.setStrokeWidth(array_stroke.get(i));//各地点での太さを取得
                // 線を描画
                canvas.drawLine(x1, y1, x2, y2, p);

                if (savedFlag == true) {
                    canvas.drawBitmap(mvBitmap, 0, 0, null);
                }
            }
        }
    }

    // タッチパネルを操作した時に呼ばれるメソッド
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 座標を取得
        int x = (int) event.getX();
        int y = (int) event.getY();
        // イベントに応じて動作を変更
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // タッチパネルが押されたとき
            case MotionEvent.ACTION_POINTER_DOWN:
                array_x.add(new Integer(x)); // 座標を配列に保存
                array_y.add(new Integer(y)); // 線の描画はしない(false)
                array_status.add(new Boolean(false));
                array_color.add(color);//その地点での色を保存
                array_r.add(red);
                array_g.add(gre);
                array_b.add(blu);
                array_stroke.add(stroke);//その地点での線の太さを保存
                invalidate(); // 画面を強制的に再描画
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP: // タッチパネルから離れたとき
            case MotionEvent.ACTION_POINTER_UP:
                array_x.add(new Integer(x)); // 座標を配列に保存
                array_y.add(new Integer(y)); // 線の描画をする(true)
                array_status.add(new Boolean(true));
                array_color.add(color);//その地点での色を保存
                array_r.add(red);
                array_g.add(gre);
                array_b.add(blu);
                array_stroke.add(stroke);//その地点での線の太さを保存
                invalidate(); // 画面を強制的に再描画
                break;
        }
        return true;
    }

    //一つ前の描画に戻すメソッド
    public void undo() {
        for (int i = array_status.size() - 1; i > 0; i--) {//描画されているところとされていないところの境界まで繰り返す
            if (!(array_status.get(i) == false && array_status.get(i - 1) == true)) {
                //境界まで各リストの中身を削除し続ける
                array_x.remove(i);
                array_y.remove(i);
                array_status.remove(i);
                array_color.remove(i);
                array_r.remove(i);
                array_g.remove(i);
                array_b.remove(i);
                array_stroke.remove(i);
            } else {
                //境界で削除を終える
                array_x.remove(i);
                array_y.remove(i);
                array_status.remove(i);
                array_color.remove(i);
                array_r.remove(i);
                array_g.remove(i);
                array_b.remove(i);
                array_stroke.remove(i);
                break;
            }
        }
        invalidate();// 画面を強制的に再描画
    }


    //全削除のメソッド
    public void delete() {
        //リストの中身を削除
        array_x.clear();
        array_y.clear();
        array_status.clear();
        array_color.clear();
        array_r.clear();
        array_g.clear();
        array_b.clear();
        array_stroke.clear();
        invalidate();//再描画して真っ白の画面にしておく
    }
}