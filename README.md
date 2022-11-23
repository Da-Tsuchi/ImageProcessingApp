## Java(Android)とOpenCVを用いた画像処理アプリ

### 実装した機能
・アンシャープマスクフィルタ(使うフィルターサイズ、重みは変更可能)\
・画像に映る物体の輪郭抽出(ガウシアンフィルタ、2値化などの前処理で実装)\
・編集した画像にペイント\
・編集後の画像をギャラリーに保存

### 注意事項
・AndroidStudioで作成\
・OpenCVのバーションは4.6.0\
・リポジトリ容量を食うので、ライブラリ(openCV)はPUSHしていない

### ライブラリのインストール手順
１．https://opencv.org/releases/ よりダウンロード(バージョンは**4.6.0、Android版**を選択)

２．ダウンロードしたzipファイルを任意のディレクトリで展開

３．AndroidStudioのプロジェクトで、左上の「File -> New -> Import Module」を選択

４．ディレクトリ選択の画面で、展開したライブラリの「OpenCV-android-sdk -> sdk」を選択(module nameはsdkからopencv-4.6.0に変更しておくとわかりやすい)

５．メニューバーから「File -> Project Structure」を選択

６．左側のメニューから「Dependencies -> app」を選択

７．右上の + アイコンをクリックして 「3: Module dependency」 を選択

８．生成された「build.gradle(:opencv-4.6.0)」の92行目　`apply plugin: 'kotlin-android'`　と、104行目　`targetSdkVersion 26`　をコメントアウトしておく

### 参考リンク
ライブラリのインストール手順\
https://laboratory.kazuuu.net/adding-the-opencv-library-to-an-android-application-using-android-studio/#toc5
