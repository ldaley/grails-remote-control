package grails.plugin.remotecontrol


import groovyx.remote.transport.http.HttpTransport
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import javax.net.ssl.HttpsURLConnection


class HttpsNoVerifyTransport extends HttpTransport {

  HttpsNoVerifyTransport(String receiverAddress, ClassLoader classLoader) {
    super(receiverAddress, classLoader)
	}

  def configureConnection(HttpURLConnection connection) {
    if (connection instanceof HttpsURLConnection) {
      setSSLNonStrict(connection)
    }
  }

  def setSSLNonStrict(HttpsURLConnection connection){
    def trustManager = [
      getAcceptedIssuers: { -> null },
      checkClientTrusted: { certs, authType -> },
      checkServerTrusted: { certs, authType -> }
    ] as X509TrustManager
    TrustManager[] trustManagerList = [trustManager]
    def sc = SSLContext.getInstance("SSL")
    sc.init(null, trustManagerList, new java.security.SecureRandom())
    connection.setSSLSocketFactory(sc.getSocketFactory())
  }

}
