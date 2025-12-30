// Bootstrap Modal インスタンスを保持するための変数
let successModalInstance;
let modalMessage;
let globalFlashMessageData;

const LIST_PAGE_URL = "/learning/list";

function openSuccessModal(message) {
  const modalElement = document.getElementById("successModal");

  // モーダルインスタンスの初期化
  if (!successModalInstance && modalElement) {
    successModalInstance = new bootstrap.Modal(modalElement, {
      backdrop: "static",
      keyboard: false,
    });
  }

  // モーダルを表示
  if (!modalMessage) {
    modalMessage = document.getElementById("modal-message");
  }

  // メッセージをモーダルボディに表示
  if (modalMessage) {
    const htmlMessage = message.replace(/\n/g, "<br>");
    modalMessage.innerHTML = htmlMessage;
  }

  // モーダルを表示
  if (successModalInstance) {
    successModalInstance.show();
  }
}

// モーダルを閉じる
function closeSuccessModal() {
  if (successModalInstance) {
    successModalInstance.hide();
  }
}

// ページロード時の初期化処理
document.addEventListener("DOMContentLoaded", function () {
  globalFlashMessageData = document.getElementById("global-flash-message-data");
  const redirectButton = document.getElementById("redirectToListViewButton");

  if (globalFlashMessageData && globalFlashMessageData.textContent.trim()) {
    const message = globalFlashMessageData.textContent.trim();

    if (message.length > 0 && message.indexOf("null") === -1) {
      console.log("Success Message Found, opening modal:", message);
      openSuccessModal(message);

      globalFlashMessageData.textContent = "";
      console.log("Flash message content cleared.");
    }
  }

  if (redirectButton) {
    redirectButton.addEventListener("click", function (event) {
      let redirectUrl = LIST_PAGE_URL;

      if (selectedMonthKey && selectedMonthKey !== "default-month") {
        redirectUrl += "?month=" + selectedMonthKey;
      }

      window.location.replace(redirectUrl);
    });
  }
});
