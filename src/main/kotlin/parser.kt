import au.com.bytecode.opencsv.CSVWriter
import com.google.gson.Gson
import org.jsoup.Jsoup
import java.io.FileWriter
import java.io.PrintWriter

class Game(val Name: String, val Url: String, val Year: String)

fun main(args: Array<String>) {
    val gamesList: MutableList<Game> = mutableListOf()
    val pagesCount = 86;

    for (page in 1..pagesCount) {

        val doc = Jsoup.connect("http://www.old-games.ru/catalog/?platform=1&sort=year&page=${page}").get()
        val games = doc.select("table.listtable tr")

        for (item in games) {
            if (item.id().contains("game")) {
                val linkElement = item.getElementsByTag("a").first()
                val year = item.child(2).text()
                val title = linkElement.attr("title")
                val url = linkElement.attr("href")

                gamesList.add(Game(title, url, year))
            }
        }
        println("Page ${page} of ${pagesCount}")
    }
    WriteCsv(gamesList)
    WriteJson(gamesList)
}

fun WriteJson(gamesList: MutableList<Game>) {
    val json = Gson().toJson(gamesList)
    PrintWriter("GAMES.json").use { out -> out.println(json) }
}

fun WriteCsv(games: List<Game>) {
    val csvWriter = CSVWriter(FileWriter("GAMES.csv", false), ';')
    for (game in games) {
        csvWriter.writeNext(arrayOf(game.Name, game.Url, game.Year))
    }
    csvWriter.close()
}