CREATE DATABASE  IF NOT EXISTS `social_ssd` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `social_ssd`;
-- MySQL dump 10.13  Distrib 5.6.13, for Win32 (x86)
--
-- Host: localhost    Database: social_ssd
-- ------------------------------------------------------
-- Server version	5.5.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `amico_di`
--

DROP TABLE IF EXISTS `amico_di`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `amico_di` (
  `Utente1` varchar(45) NOT NULL,
  `Utente2` varchar(45) NOT NULL,
  `Accettata` int(11) DEFAULT NULL,
  PRIMARY KEY (`Utente1`,`Utente2`),
  KEY `fk_2_idx` (`Utente2`),
  CONSTRAINT `fk_1` FOREIGN KEY (`Utente1`) REFERENCES `utente` (`Username`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_2` FOREIGN KEY (`Utente2`) REFERENCES `utente` (`Username`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `amico_di`
--

LOCK TABLES `amico_di` WRITE;
/*!40000 ALTER TABLE `amico_di` DISABLE KEYS */;
INSERT INTO `amico_di` VALUES ('a','giuseppe',1),('antonella','chiara',1),('antonella','elisa',1),('antonella','giuseppe',1),('antonella','maurizio',1),('antonella','xhensila',1),('elisa','chiara',1),('giulia','antonella',0),('giulia','miranda',1),('giulia','xhensila',1),('jenny','giulia',1),('jenny','xhensila',1),('linda','xhensila',1),('maurizio','a',1),('maurizio','chiara',1),('maurizio','elisa',1),('maurizio','giulia',1),('rosy','auryn',0),('rosy','xhensila',1),('xhensila','miranda',1);
/*!40000 ALTER TABLE `amico_di` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bacheca`
--

DROP TABLE IF EXISTS `bacheca`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bacheca` (
  `idBacheca` int(11) NOT NULL AUTO_INCREMENT,
  `Utente` varchar(45) DEFAULT NULL,
  `Contenuto` varchar(45) DEFAULT NULL,
  `Tipo` int(11) DEFAULT NULL,
  `Data` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idBacheca`),
  KEY `fk_!_idx` (`Utente`),
  CONSTRAINT `fk_!` FOREIGN KEY (`Utente`) REFERENCES `utente` (`Username`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bacheca`
--

LOCK TABLES `bacheca` WRITE;
/*!40000 ALTER TABLE `bacheca` DISABLE KEYS */;
INSERT INTO `bacheca` VALUES (49,'xhensila','Oggi è una bellssima giornata!',1,'2014-06-25 18:23:09'),(50,'xhensila','Finalmente è venerdì!',1,'2014-06-25 18:23:41'),(51,'miranda','Ma che bel sole che c\'è oggi!!',1,'2014-06-25 18:24:39'),(52,'giulia','Ciao a tutti!!! ',1,'2014-06-25 18:28:28'),(53,'miranda','Si parte per le vacanze!!!',1,'2014-06-25 18:44:45');
/*!40000 ALTER TABLE `bacheca` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `commenti`
--

DROP TABLE IF EXISTS `commenti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `commenti` (
  `idCommento` int(11) NOT NULL AUTO_INCREMENT,
  `Utente` varchar(45) DEFAULT NULL,
  `Contenuto` varchar(45) DEFAULT NULL,
  `Data` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `idBacheca` int(11) DEFAULT NULL,
  PRIMARY KEY (`idCommento`),
  KEY `FK1_idx` (`idBacheca`),
  KEY `fk2_idx` (`Utente`),
  CONSTRAINT `FK1` FOREIGN KEY (`idBacheca`) REFERENCES `bacheca` (`idBacheca`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk2` FOREIGN KEY (`Utente`) REFERENCES `utente` (`Username`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `commenti`
--

LOCK TABLES `commenti` WRITE;
/*!40000 ALTER TABLE `commenti` DISABLE KEYS */;
INSERT INTO `commenti` VALUES (12,'miranda','si davvero!! Usciamo stasera?','2014-06-25 18:24:24',50),(13,'miranda','Andiamo al parco?','2014-06-25 18:24:51',49);
/*!40000 ALTER TABLE `commenti` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utente`
--

DROP TABLE IF EXISTS `utente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `utente` (
  `Username` varchar(45) NOT NULL,
  `Password` varchar(45) DEFAULT NULL,
  `Immagine` varchar(200) DEFAULT NULL,
  `Email` varchar(200) DEFAULT NULL,
  `Ip` varchar(45) DEFAULT NULL,
  `Port` int(11) DEFAULT NULL,
  `Data` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utente`
--

LOCK TABLES `utente` WRITE;
/*!40000 ALTER TABLE `utente` DISABLE KEYS */;
INSERT INTO `utente` VALUES ('a','a','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\a.jpg','userdefault@gmail.com','192.168.150.1',1001,'2014-06-22 15:25:02'),('antonella','antonella','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\antonella.jpg','antonella45@libero.it','192.168.150.1',9999,'2014-06-22 15:25:02'),('arianna','arianna','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\arianna_profilo.jpg','arianna87@gmail.com','192.168.150.1',7000,'2014-06-22 15:25:02'),('auryn','auryn','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\auryn_profilo.jpg','auryn@gmail.com','192.168.150.1',6666,'2014-06-22 20:03:40'),('chiara','chiara','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\chiara.jpg','chiara@gmail.com','192.168.150.1',6666,'2014-06-22 15:25:02'),('elisa','elisa','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\elisa.jpg','elisa@gmail.com','192.168.150.1',5550,'2014-06-22 15:25:02'),('giulia','giulia','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\giulia_profilo.jpg','giulia@gmail.com','192.168.150.128',5555,'2014-06-25 17:46:29'),('giuseppe','giuseppe','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\giuseppe.jpg','giuseppe@hotmail.com','192.168.150.1',1001,'2014-06-22 15:25:02'),('jenny','jenny','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\jenny_profilo.jpg','jenny@gmail.com','192.168.150.1',6666,'2014-06-22 20:00:53'),('linda','linda','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\linda_profilo.jpg','linda@gmail.com','192.168.150.1',6666,'2014-06-22 20:10:22'),('maurizio','maurizio','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\maurizio.jpg','maurizio_prova@gmail.com','192.168.150.1',9001,'2014-06-22 15:25:02'),('miranda','miranda','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\miranda_profilo.jpg','miranda@gmail.com','192.168.150.1',6666,'2014-06-22 20:03:26'),('rosy','rosy','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\rosy_profilo.jpg','rosy@gmail.com','192.168.150.1',6666,'2014-06-22 19:59:55'),('valentina','valentina','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\valentina_profilo.jpg','valentinaaaa@hotmail.com','192.168.150.1',7777,'2014-06-22 15:25:02'),('xhensila','xhensila','C:\\Users\\xhensila\\workspace_xf\\ServerRMI_Final\\img\\xhensila_profilo.jpg','xhensila@gmail.com','192.168.150.1',4444,'2014-06-22 15:25:02');
/*!40000 ALTER TABLE `utente` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-06-27 10:21:51
