public class Items {
  public static void main(String[] args) {
    // 配列：Todoを3件、0番から順に並べて持つ入れ物
    String[] todos = { "牛乳を買う", "卵を買う", "パンを買う", "掃除をする" };

    // 繰り返し：i を 0 → 1 → 2 と進めながら、同じ1行を件数ぶん実行する
    for (int i = 0; i < todos.length; i++) {
      System.out.println("<li>" + todos[i] + "</li>");
    }
  }
}
