package kpn.core.db.couch

import kpn.core.db.Doc
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate

class DatabaseSave(context: DatabaseContext) {

  def save[T](doc: Doc): Unit = {

    val url = s"${context.databaseUrl}/${doc._id}"
    val json = context.objectMapper.writeValueAsString(doc)

    val restTemplate = new RestTemplate
    restTemplate.getInterceptors.add(
      new BasicAuthenticationInterceptor(context.couchConfig.user, context.couchConfig.password)
    )

    try {
      val entity = new HttpEntity[String](json)
      val response: ResponseEntity[String] = restTemplate.exchange(url, HttpMethod.PUT, entity, classOf[String])
      if (!HttpStatus.CREATED.equals(response.getStatusCode)) {
        throw new IllegalStateException(s"Could not save '$url' (unexpected status code ${response.getStatusCode.name()})")
      }
    }
    catch {
      case e: ResourceAccessException =>
        throw new IllegalStateException(s"Could not save '$url' (invalid user/password?)", e)

      case e: HttpClientErrorException =>
        if (HttpStatus.CONFLICT.equals(e.getStatusCode)) {
          throw new IllegalStateException(s"Could not save '$url' (_rev mismatch)", e)
        }
        if (HttpStatus.UNAUTHORIZED.equals(e.getStatusCode)) {
          throw new IllegalStateException(s"Could not save '$url' (invalid user/password?)", e)
        }
        throw new IllegalStateException(s"Could not save '$url'", e)
    }
  }

}
