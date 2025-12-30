document.addEventListener("DOMContentLoaded", function () {
  const fileInput = document.getElementById("profile-file");
  const displayElement = document.getElementById("file-name");

  // 要素が存在する場合のみ、イベントリスナーを設定する
  if (fileInput && displayElement) {
    fileInput.addEventListener("change", function () {
      if (this.files && this.files.length > 0) {
        const fileName = this.files[0].name;
        displayElement.textContent = fileName;
      } else {
        // ファイルが選択されなかった場合
        displayElement.textContent = "選択されていません";
      }
    });
  } else {
    // 該当する要素がないページ（グラフ画面など）では何もしない
    console.log("File upload elements not found. Skipping listener setup.");
  }
});
