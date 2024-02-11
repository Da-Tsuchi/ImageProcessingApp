## Pixel Craft Studio
<img src="https://img.shields.io/badge/-Java-red.svg?logo=Java&style=flat" /><img src="https://img.shields.io/badge/-Android Studio-black.svg?logo=Java&style=flat" />

### アプリ概要
- 画像処理の授業で学んだ技術を実装し、スマホやタブレットの写真で簡単に実行できるAndroidアプリ
- <a href="https://alss-portal.gifu-u.ac.jp/campusweb/slbssbdr.do?value(risyunen)=2022&value(semekikn)=1&value(kougicd)=1TDB8343A0&value(crclumcd)=T-2022)">画像処理授業シラバス</a>

### 実装した機能
#### アンシャープマスクフィルタ(使うフィルターサイズ、重みは変更可能)
  - 画像のエッジが強調された画像を得るための処理
  - 最適なフィルターサイズ、重みを見つける必要がある
    
  <video src="https://github.com/Da-Tsuchi/ImageProcessingApp/assets/117258037/63d0630f-16a9-403a-99ef-429d12a0815b"></video>
      
#### 画像に映る物体の輪郭抽出
  - カラー画像をグレースケールに変換
  - グレースケール画像にガウシアンフィルタをかけることでぼかす
  - 大津の2値化
  - 輪郭を取り出す

  <video src=https://github.com/Da-Tsuchi/ImageProcessingApp/assets/117258037/ce174a84-a9b3-48de-b4dd-b190ed074955></video>
  <video src=https://github.com/Da-Tsuchi/ImageProcessingApp/assets/117258037/059bedbc-ce2d-49a3-9c13-ede5093f9e74></video>
  
  
#### 編集した画像にペイント・編集後の画像をギャラリーに保存
  <video src=https://github.com/Da-Tsuchi/ImageProcessingApp/assets/117258037/9c329c03-e4f0-4cd5-98fc-a244b8e0fa7f></video>
  



### 使用した技術
- Java(Android Studio)
- OpenCV 4.6.0

### 注意事項
- リポジトリ容量を食うので、ライブラリ(openCV)はPUSHしていない

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
