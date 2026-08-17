import com.sun.net.httpserver.HttpServer; // Webサーバー本体を使えるようにします。
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定できるようにします。
import java.net.URLDecoder; // 追加: フォームから送られた日本語などを元の文字へ戻すために使います。
import java.nio.charset.StandardCharsets; // UTF-8という文字コードを安全に指定できるようにします。
import java.util.ArrayList; // 追加: 後からTodoを追加できる空のListを作るために使います。
import java.util.List; // ★変更: Todoクラスのデータを一覧として持つListを使えるようにします。

public class App { // Appという名前のプログラム本体を定義します。
  private static final List<Todo> todos = new ArrayList<>(); // ★変更: Todoを貯めるListをmainの外に置きます。
  private static int nextId = 1; // ★変更: 次に振るidをmainの外に置き、1から始めます。

  private static class Todo { // ★変更: 1件分のid、title、doneをまとめるTodoクラスです。
    private int id; // ★変更: Todoが何番かを保存します。
    private String title; // ★変更: やることの文字を保存します。
    private boolean done; // ★変更: 終わったかどうかをtrueまたはfalseで保存します。

    private Todo(int id, String title, boolean done) { // ★変更: newでTodoを作るときに3つの値を受け取ります。
      this.id = id; // ★変更: 受け取ったidを保存します。
      this.title = title; // ★変更: 受け取ったtitleを保存します。
      this.done = done; // ★変更: 受け取った完了状態を保存します。
    } // ★変更: Todoを作る処理の終わりです。

    public int getId() { // ★変更: 保存されているidを読み出すメソッドです。
      return id; // ★変更: idの値を呼び出し元へ返します。
    } // ★変更: getIdメソッドの終わりです。

    public String getTitle() { // ★変更: 保存されているtitleを読み出すメソッドです。
      return title; // ★変更: titleの値を呼び出し元へ返します。
    } // ★変更: getTitleメソッドの終わりです。

    public boolean isDone() { // ★変更: 保存されているdoneを読み出すメソッドです。
      return done; // ★変更: doneの値を呼び出し元へ返します。
    } // ★変更: isDoneメソッドの終わりです。

    public void setDone(boolean done) { // ★変更: doneを書き換えるメソッドです。
      this.done = done; // ★変更: 受け取った値でdoneを書き換えます。
    } // ★変更: setDoneメソッドの終わりです。
  } // ★変更: Todoクラスの終わりです。

  private static Integer readId(String query) { // ★追加: URLの「?id=数字」からidを読み取ります。
    if (query == null) { // ★追加: 「?」以降がなく、idも付いていないか確認します。
      return null; // ★追加: idがないことを呼び出し元へ伝えます。
    } // ★追加: 「?」以降がない場合の処理の終わりです。
    for (String parameter : query.split("&")) { // ★追加: URLに付いた項目を1件ずつ確認します。
      String[] pair = parameter.split("=", 2); // ★追加: 項目名と値を最初の「=」で分けます。
      if (!pair[0].equals("id")) { // ★追加: id以外の項目かどうかを確認します。
        continue; // ★追加: id以外なら次の項目を確認します。
      } // ★追加: 項目名を確認する処理の終わりです。
      if (pair.length < 2) { // ★追加: 「id=」の値がない場合を確認します。
        return null; // ★追加: 有効なidがないことを伝えます。
      } // ★追加: idの値がない場合の処理の終わりです。
      try { // ★追加: idを数字へ変換できるか試します。
        return Integer.valueOf(URLDecoder.decode(pair[1], StandardCharsets.UTF_8)); // ★追加: idの値を元の文字へ戻して整数にします。
      } catch (IllegalArgumentException e) { // ★追加: 値が数字でない場合などを受け止めます。
        return null; // ★追加: 不正なidとして、何も変更しないためnullを返します。
      } // ★追加: idを数字へ変換する処理の終わりです。
    } // ★追加: URLに付いた項目を確認する処理の終わりです。
    return null; // ★追加: id項目が見つからなかったことを伝えます。
  } // ★追加: URLからidを読み取る処理の終わりです。

  private static String escapeHtml(String text) { // 追加: 入力文字がHTMLの命令として扱われないように変換します。
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
        "&#39;"); // 追加: HTMLで特別な意味を持つ文字を安全な表記へ置き換えます。
  } // 追加: HTML用の安全な文字へ変換する処理の終わりです。

  public static void main(String[] args) throws Exception { // Javaが最初に実行する処理です。
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでWebサーバーを作ります。
    todos.add(new Todo(nextId++, "牛乳を買う", false)); // ★変更: idが1で未完了のサンプルをnewで作り、Listへ入れます。
    todos.add(new Todo(nextId++, "卵を買う", true)); // ★変更: idが2で完了済みのサンプルをnewで作り、Listへ入れます。
    server.createContext("/", exchange -> { // 「/」へのアクセスを受けたときの処理を直接書きます。
      String path = exchange.getRequestURI().getPath(); // 追加: アクセスされたパスを調べます。
      String method = exchange.getRequestMethod(); // 追加: GETかPOSTかという、アクセス方法を調べます。
      String message = "Hello, Server!"; // ブラウザへ返す文字をmessage変数に入れます。
      String contentType = "text/plain; charset=UTF-8"; // 追加: 「/」以外は今までどおり普通の文字として返します。
      if (path.equals("/") && method.equals("GET")) { // 追加: 「/」を開いた場合に、入力フォームとTodo一覧を作ります。
        int doneCount = 0; // ★変更: 完了したTodoの件数を表示するため、数を入れる変数を用意します。
        for (Todo todo : todos) { // ★変更: Todoを1件ずつ確認します。
          if (todo.isDone()) { // ★変更: 完了済みのTodoかを確認します。
            doneCount++; // ★変更: 完了済みなら件数を1増やします。
          } // ★変更: 完了状態を確認する処理の終わりです。
        } // ★変更: 完了件数を数える処理の終わりです。

        String html = "<!DOCTYPE html><html lang='ja'><head>"; // ★変更: 日本語のHTMLページを始めます。
        html += "<meta charset='UTF-8'>"; // ★変更: 日本語を正しく表示するためUTF-8を指定します。
        html += "<meta name='viewport' content='width=device-width, initial-scale=1'>"; // ★変更: スマートフォンでも画面幅を正しく使います。
        html += "<title>My Todo List</title>"; // ★変更: ブラウザのタブに表示する名前です。
        html += "<style>"; // ★変更: ここから画面の色や配置を指定するCSSを始めます。
        html += ":root{color-scheme:light;font-family:'Segoe UI','Yu Gothic UI',sans-serif;}"; // ★変更: 読みやすい書体を指定します。
        html += "*{box-sizing:border-box;}"; // ★変更: 余白を含めて部品の大きさを計算します。
        html += "body{margin:0;min-height:100vh;display:grid;place-items:center;padding:32px 16px;color:#172033;background:radial-gradient(circle at 15% 15%,#dbeafe 0,transparent 32%),radial-gradient(circle at 85% 85%,#ede9fe 0,transparent 34%),linear-gradient(135deg,#f8fafc,#eef2ff);}"; // ★変更: 画面全体にやわらかな背景を付けます。
        html += ".todo-card{width:min(100%,680px);padding:36px;background:rgba(255,255,255,.94);border:1px solid rgba(255,255,255,.8);border-radius:28px;box-shadow:0 24px 70px rgba(51,65,85,.16);backdrop-filter:blur(12px);}"; // ★変更: Todo全体を白いカードとして表示します。
        html += ".header{display:flex;align-items:flex-end;justify-content:space-between;gap:24px;margin-bottom:28px;}"; // ★変更: 見出しと件数を横に並べます。
        html += ".eyebrow{margin:0 0 6px;color:#6366f1;font-size:12px;font-weight:800;letter-spacing:.16em;}"; // ★変更: 小さな見出しを青紫色にします。
        html += "h1{margin:0;font-size:clamp(30px,6vw,44px);line-height:1.1;letter-spacing:-.04em;}"; // ★変更: 主見出しを大きく表示します。
        html += ".summary{display:flex;gap:8px;flex-wrap:wrap;justify-content:flex-end;}"; // ★変更: 件数表示を横に並べます。
        html += ".summary span{padding:7px 11px;border-radius:999px;background:#eef2ff;color:#4f46e5;font-size:13px;font-weight:700;white-space:nowrap;}"; // ★変更: 件数を丸いラベルにします。
        html += ".add-form{display:grid;grid-template-columns:minmax(0,1fr) auto;gap:10px;margin-bottom:26px;padding:8px;background:#f1f5f9;border:1px solid #e2e8f0;border-radius:17px;}"; // ★変更: 入力欄と追加ボタンをひとまとまりにします。
        html += ".add-form input{min-width:0;padding:13px 14px;border:0;outline:0;background:transparent;color:#172033;font:inherit;}"; // ★変更: 入力欄を広く読みやすくします。
        html += ".add-form input::placeholder{color:#94a3b8;}"; // ★変更: 入力例を控えめな色にします。
        html += ".add-form button{padding:12px 22px;border:0;border-radius:12px;color:white;background:linear-gradient(135deg,#6366f1,#8b5cf6);font:inherit;font-weight:800;cursor:pointer;box-shadow:0 8px 18px rgba(99,102,241,.25);transition:transform .15s,box-shadow .15s;}"; // ★変更: 追加ボタンを目立たせます。
        html += ".add-form button:hover{transform:translateY(-1px);box-shadow:0 10px 22px rgba(99,102,241,.32);}"; // ★変更: マウスを重ねたとき少し浮かせます。
        html += ".todo-list{display:grid;gap:11px;margin:0;padding:0;list-style:none;}"; // ★変更: Todo同士の間隔を整えます。
        html += ".todo-item{display:flex;align-items:center;justify-content:space-between;gap:18px;padding:16px 17px;border:1px solid #e2e8f0;border-radius:16px;background:#fff;box-shadow:0 5px 14px rgba(15,23,42,.04);}"; // ★変更: Todoを1件ずつ小さなカードにします。
        html += ".todo-item.is-done{border-color:#bbf7d0;background:#f0fdf4;}"; // ★変更: 完了済みのTodoを薄い緑色にします。
        html += ".todo-title{min-width:0;overflow-wrap:anywhere;font-weight:700;line-height:1.5;}"; // ★変更: 長いtitleもカード内で折り返します。
        html += ".is-done .todo-title{color:#15803d;}"; // ★変更: 完了済みのtitleを緑色にします。
        html += ".actions{display:flex;gap:7px;flex-shrink:0;}"; // ★変更: 完了と削除を横に並べます。
        html += ".action{padding:8px 11px;border-radius:10px;text-decoration:none;font-size:13px;font-weight:800;transition:background .15s,transform .15s;}"; // ★変更: 操作リンクをボタンのようにします。
        html += ".action:hover{transform:translateY(-1px);}"; // ★変更: 操作リンクへマウスを重ねたとき少し浮かせます。
        html += ".done-action{color:#047857;background:#ecfdf5;}"; // ★変更: 完了リンクを緑系にします。
        html += ".done-action:hover{background:#d1fae5;}"; // ★変更: 完了リンクへマウスを重ねたとき色を濃くします。
        html += ".delete-action{color:#be123c;background:#fff1f2;}"; // ★変更: 削除リンクを赤系にします。
        html += ".delete-action:hover{background:#ffe4e6;}"; // ★変更: 削除リンクへマウスを重ねたとき色を濃くします。
        html += ".empty{margin:0;padding:30px;text-align:center;color:#64748b;border:1px dashed #cbd5e1;border-radius:16px;}"; // ★変更: Todoがない場合の案内を整えます。
        html += ":focus-visible{outline:3px solid rgba(99,102,241,.35);outline-offset:3px;}"; // ★変更: キーボード操作中の選択位置を見やすくします。
        html += "@media(max-width:560px){.todo-card{padding:25px 18px;border-radius:22px}.header{align-items:flex-start;flex-direction:column;gap:14px}.summary{justify-content:flex-start}.add-form{grid-template-columns:1fr}.add-form button{width:100%}.todo-item{align-items:flex-start;flex-direction:column}.actions{width:100%}.action{flex:1;text-align:center}}"; // ★変更: スマートフォンでは縦並びにします。
        html += "</style></head><body><main class='todo-card'>"; // ★変更: CSSを閉じ、画面のカードを始めます。
        html += "<header class='header'><div><p class='eyebrow'>MY TODO LIST</p><h1>今日のTodo</h1></div>"; // ★変更: 画面の見出しを表示します。
        html += "<div class='summary'><span>全 " + todos.size() + " 件</span><span>完了 " + doneCount + " 件</span></div></header>"; // ★変更: 全件数と完了件数を表示します。
        html += "<form class='add-form' method='post' action='/add'>"; // ★変更: 入力内容をPOSTで「/add」へ送るフォームを始めます。
        html += "<input type='text' name='todo' placeholder='やることを入力…' aria-label='新しいTodo'>"; // ★変更: 入力例と読み上げ用の説明を付けます。
        html += "<button type='submit'>追加する</button>"; // ★変更: 入力したTodoを送るボタンです。
        html += "</form>"; // ★変更: 入力フォームを閉じます。
        html += "<ul class='todo-list'>"; // ★変更: Todo一覧を表示する箇条書きを始めます。
        for (Todo todo : todos) { // ★変更: List<Todo>からTodoを1件ずつ順番に取り出します。
          String doneMark = todo.isDone() ? " ✔" : ""; // ★変更: doneがtrueのTodoにだけチェック印を用意します。
          String itemClass = todo.isDone() ? "todo-item is-done" : "todo-item"; // ★変更: 完了状態に合わせて見た目を切り替えます。
          String safeTitle = escapeHtml(todo.getTitle()); // ★変更: titleをHTMLで安全に表示できる文字へ変換します。
          html += "<li class='" + itemClass + "'><span class='todo-title'>" + safeTitle + doneMark + "</span>"; // ★変更: titleと完了時の「 ✔」を表示します。
          html += "<span class='actions'>"; // ★変更: 操作リンクをまとめる部分を始めます。
          html += "<a class='action done-action' href='/done?id=" + todo.getId() + "' aria-label='" + safeTitle + "を完了にする'>完了</a>"; // ★変更: id入りの完了リンクをボタン風に表示します。
          html += "<a class='action delete-action' href='/delete?id=" + todo.getId() + "' aria-label='" + safeTitle + "を削除する'>削除</a>"; // ★変更: id入りの削除リンクをボタン風に表示します。
          html += "</span></li>"; // ★変更: 操作部分とTodo項目を閉じます。
        } // 追加: Todoを順番に取り出す処理の終わりです。
        html += "</ul>"; // ★変更: HTMLの箇条書きを閉じます。
        if (todos.isEmpty()) { // ★変更: Todoが1件もないか確認します。
          html += "<p class='empty'>Todoはまだありません。上の入力欄から追加できます。</p>"; // ★変更: 空の一覧に案内を表示します。
        } // ★変更: Todoがない場合の処理の終わりです。
        html += "</main></body></html>"; // ★変更: カードとHTMLページを閉じます。
        message = html; // 追加: 組み立てたHTMLをブラウザへ返す文字にします。
        contentType = "text/html; charset=UTF-8"; // 追加: 「/」だけをUTF-8のHTMLとして返します。
      } else if (path.equals("/add") && method.equals("POST")) { // 追加: フォームから「/add」へ送られた場合にTodoを受け取ります。
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 追加:
                                                                                                           // 送られたフォームデータをUTF-8で読み取ります。
        String newTodo = ""; // 追加: フォームから取り出す新しいTodoの入れ物を用意します。
        for (String parameter : requestBody.split("&")) { // 追加: 送られた項目を1件ずつ確認します。
          String[] pair = parameter.split("=", 2); // 追加: 項目名と入力値を、最初の「=」で分けます。
          String name = URLDecoder.decode(pair[0], StandardCharsets.UTF_8); // 追加: 項目名をURLDecoderで元の文字へ戻します。
          if (name.equals("todo")) { // 追加: todoという名前の入力欄を見つけたか確認します。
            String encodedValue = pair.length > 1 ? pair[1] : ""; // 追加: 入力値がない場合も安全に空文字として扱います。
            newTodo = URLDecoder.decode(encodedValue, StandardCharsets.UTF_8); // 追加: 日本語を含む入力値をURLDecoderで元の文字へ戻します。
            break; // 追加: todoの値を見つけたので、項目を探す処理を終えます。
          } // 追加: todoという名前かを確認する処理の終わりです。
        } // 追加: 送られた項目を確認する処理の終わりです。
        if (!newTodo.trim().isEmpty()) { // 追加: 空文字や空白だけの入力ではないことを確認します。
          todos.add(new Todo(nextId++, newTodo, false)); // ★変更: newでTodoを作り、順番のidとfalseを付けてListへ追加します。
        } // 追加: 空の入力を除外する処理の終わりです。
        exchange.getResponseHeaders().set("Location", "/"); // 追加: Todo追加後の移動先が「/」であることをブラウザへ伝えます。
        exchange.sendResponseHeaders(303, -1); // 追加: 303を返し、ブラウザを「/」へ移動させます。
        exchange.close(); // 追加: 「/add」への応答を閉じます。
        return; // 追加: この後に通常の文字を返さないよう、ここで処理を終えます。
      } else if (path.equals("/done") && method.equals("GET")) { // ★追加: 完了リンクからのGETアクセスを処理します。
        Integer id = readId(exchange.getRequestURI().getRawQuery()); // ★追加: URLに書かれたidを読み取ります。
        if (id != null) { // ★追加: idが付いていて、数字として読めた場合だけ変更します。
          for (Todo todo : todos) { // ★追加: 保存中のTodoを1件ずつ確認します。
            if (todo.getId() == id) { // ★変更: getIdで読み出したidがURLのidと一致するか確認します。
              todo.setDone(true); // ★変更: setDoneを使い、一致した1件を完了済みへ変更します。
              break; // ★追加: 1件だけ変更したので、探す処理を終えます。
            } // ★追加: idが一致するか確認する処理の終わりです。
          } // ★追加: 完了するTodoを探す処理の終わりです。
        } // ★追加: 有効なidがある場合の処理の終わりです。
        exchange.getResponseHeaders().set("Location", "/"); // ★追加: 処理後の移動先が「/」であることをブラウザへ伝えます。
        exchange.sendResponseHeaders(303, -1); // ★追加: 303を返してブラウザを「/」へ移動させます。
        exchange.close(); // ★追加: 「/done」への応答を閉じます。
        return; // ★追加: この後に通常の文字を返さないよう、ここで処理を終えます。
      } else if (path.equals("/delete") && method.equals("GET")) { // ★追加: 削除リンクからのGETアクセスを処理します。
        Integer id = readId(exchange.getRequestURI().getRawQuery()); // ★追加: URLに書かれたidを読み取ります。
        if (id != null) { // ★追加: idが付いていて、数字として読めた場合だけ変更します。
          for (int i = 0; i < todos.size(); i++) { // ★追加: List内の位置を使い、Todoを1件ずつ確認します。
            if (todos.get(i).getId() == id) { // ★変更: getIdで読み出したidがURLのidと一致するか確認します。
              todos.remove(i); // ★追加: 一致した1件をListから取り除きます。
              break; // ★追加: 1件だけ削除したので、探す処理を終えます。
            } // ★追加: idが一致するか確認する処理の終わりです。
          } // ★追加: 削除するTodoを探す処理の終わりです。
        } // ★追加: 有効なidがある場合の処理の終わりです。
        exchange.getResponseHeaders().set("Location", "/"); // ★追加: 処理後の移動先が「/」であることをブラウザへ伝えます。
        exchange.sendResponseHeaders(303, -1); // ★追加: 303を返してブラウザを「/」へ移動させます。
        exchange.close(); // ★追加: 「/delete」への応答を閉じます。
        return; // ★追加: この後に通常の文字を返さないよう、ここで処理を終えます。
      } // 追加: パスとアクセス方法による分岐の終わりです。
      byte[] response = message.getBytes(StandardCharsets.UTF_8); // 文字をUTF-8形式のデータへ変換します。
      exchange.getResponseHeaders().set("Content-Type", contentType); // 追加: パスに応じて、普通の文字またはHTMLとして返すことをブラウザへ伝えます。
      exchange.sendResponseHeaders(200, response.length); // 成功を表す200と、返すデータの長さを送ります。
      try (var output = exchange.getResponseBody()) { // 返事を書き込む場所を、処理後に自動で閉じる形で開きます。
        output.write(response); // ブラウザへ文字のデータを送ります。
      } // 返事を書き込む場所をここで閉じます。
    }); // 「/」へのアクセス処理の登録を終えます。
    server.start(); // Webサーバーを起動して、アクセスを待ちます。
    System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 指定された案内をターミナルへそのまま表示します。
  } // mainメソッドの終わりです。
} // Appクラスの終わりです。
