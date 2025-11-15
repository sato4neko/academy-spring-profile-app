function changeContent() {
  //IDを取得
  const selectElement = document.getElementById("month-select");
  const selectedMonth = selectElement.value;

  // コンテンツを非表示
  // elはコールバック関数に渡される最初の引数 ※勉強用
  document.querySelectorAll(".display_content").forEach((el) => {
    el.style.display = "none";
  });

  // コンテンツを表示
  const contentToShow = document.getElementById(selectedMonth);
  if (contentToShow) {
    contentToShow.style.display = "block";
  }
}
