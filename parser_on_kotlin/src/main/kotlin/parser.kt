import au.com.bytecode.opencsv.CSVWriter
import com.google.gson.Gson
import org.jsoup.Jsoup
import java.io.FileWriter
import java.io.PrintWriter
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier
import javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager

class Game(val Name: String, val Genre: String, val Url: String, val Year: String)

//todo очень нехорошо принимать все подряд сертификаты, но я пока не готов написать нормальный код в этом месте >_>
@Throws(KeyManagementException::class, NoSuchAlgorithmException::class)
fun enableSSLSocket() {
    setDefaultHostnameVerifier(object : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    })

    val context = SSLContext.getInstance("TLS")
    context.init(null, arrayOf<X509TrustManager>(elements = object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate?> {
            return arrayOfNulls<X509Certificate?>(0)
        }

        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }
    }), SecureRandom())
    setDefaultSSLSocketFactory(context.getSocketFactory())
}

fun main(args: Array<String>) {
    val gamesList: MutableList<Game> = mutableListOf()
    val pagesCount = 89;

    enableSSLSocket();

    for (page in 1..pagesCount) {

        val doc = Jsoup.connect("https://www.old-games.ru/catalog/?platform=1&sort=year&page=${page}").get()
        val games = doc.select("table.listtable tr")

        for (item in games) {
            if (item.id().contains("game")) {
                val linkElement = item.getElementsByTag("a").first()
                val genre = item.child(1).text()
                val year = item.child(2).text()
                val title = linkElement.attr("title")
                val url = linkElement.attr("href")

                gamesList.add(Game(title, genre, url, year))
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
        csvWriter.writeNext(arrayOf(game.Name, game.Genre, game.Url, game.Year))
    }
    csvWriter.close()
}