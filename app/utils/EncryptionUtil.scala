package utils

import org.jasypt.util.text.BasicTextEncryptor
import org.jasypt.exceptions.AlreadyInitializedException

object EncryptionUtil {

  val KEY : String = "y3A-1-g0T-a-r4sH!!!!!!!!"
  val textEncryptor = new BasicTextEncryptor()

  def encrypt(unencryptedString : String) = {
    initialize()
    textEncryptor.encrypt(unencryptedString)
  }

  def decrypt(encryptedString : String) = {
    initialize()
    textEncryptor.decrypt(encryptedString)
  }

  def initialize() = {
    try {
      textEncryptor.setPassword(KEY)
    } catch {
      case aie: AlreadyInitializedException =>
      case e: Exception =>
    }
  }

}
