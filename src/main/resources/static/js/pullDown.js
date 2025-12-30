// プルダウンの変更時に呼び出される関数
function changeContent() {
  const selectElement = document.getElementById("month-select");
  const selectedMonth = selectElement.value; // YYYY-MM形式

  // 選択された月をURLパラメータとしてリダイレクトする
  window.location.href = "/learning/list?month=" + selectedMonth;
}
