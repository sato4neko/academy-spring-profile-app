document.addEventListener("DOMContentLoaded", function () {
  const fileInput = document.getElementById("profile-file");
  const displayElement = document.getElementById("file-name");

  fileInput.addEventListener("change", function () {
    if (this.files && this.files.length > 0) {
      const fileName = this.files[0].name;

      displayElement.textContent = fileName;
    } else {
      // ファイルが選択されなかった場合
      displayElement.textContent = "選択されていません";
    }
  });
});
