import org.apache.spark.mllib.linalg.{DenseVector, Matrix, Vector}
import org.apache.spark.mllib.feature.Normalizer
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.ml.feature.{RegexTokenizer, Tokenizer}
import org.apache.spark.sql.SparkSession
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.sql.functions._


// Define the  schema 

case class Credit(Creditability: Double,Balance: Double, Duration: Double, History: Double, Purpose: Double, Amount: Double,
    Savings: Double, Employment: Double, InstPercent: Double, sexMarried: Double, Guarantors: Double,
    ResidenceDuration: Double, Assets: Double, Age: Double, ConcCredit: Double, Apartment: Double,
    Credits: Double, Occupation: Double, Dependents:Double, HasPhone: Double, Foreign: Double
  )
  
  
object ProjetSparkCode {
  
   def main(args: Array[String]) {
     Logger.getLogger("org").setLevel(Level.OFF)
     Logger.getLogger("akka").setLevel(Level.OFF)

    val conf = new SparkConf()
    conf.setAppName("SparSQLO").setMaster("local")
    println("Debut du programme")
     
 
    // Define Spark Context
    val sc = new SparkContext(conf)
     
    // SQLContext entry point pour les donnees structures
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
     
    // Conversion of RDD to DataFrame
    import sqlContext.implicits._
  
  // Import file, create RDD and conversion to dataframe
     
     val dfClients = sc.textFile("/home/cloudera/FileCredit.csv").map(_.split(",")).map(p => Credit(p(0).toDouble,p(1).toDouble,p(2).toDouble,p(3).toDouble,p(4).toDouble,p(5).toInt,p(6).toDouble,p(7).toDouble,p(8).toDouble,p(9).toDouble,p(10).toDouble,p(11).toDouble,p(12).toDouble,p(13).toDouble,p(14).toDouble,p(15).toDouble,p(16).toDouble,p(17).toDouble,p(18).toDouble,p(19).toDouble,p(20).toDouble)).toDF()

          
    // Register DataFrame as a table.
    dfClients.createOrReplaceTempView("TableSQLClients")
    
    // Print the schema schema
    println("Le schema du RDD")
    dfClients.printSchema()
    
    // Dataframe content
     println("Afficher le contenu du dataframe");
     dfClients.first()
     println("Afficher les 25 premiers enregistrements du dataframe");
     dfClients.show(25)
     
     //Describe numerical variables
     println("Description des variables numeriques");
     dfClients.describe("Duration","Age","Amount").show
     
     //Use of group by
     println("Groupement par Age et par Creditability");
     dfClients.groupBy("Creditability", "Age").count.show
     println("Groupement par variable (etranger ou pas) et par Creditability");
     dfClients.groupBy("Creditability", "Foreign").count.show
     
     //Use of filter
     println("Filtrage par rapport a la variable Amount");
     val highAmount= dfClients.filter("Amount > 1000")
     highAmount.show()
     println("Filtrage par rapport a la variable Age");
     val lowerAge= dfClients.filter("Age <30")
     lowerAge.show()
     
     //Some requests
     //Find ages having maximum duration   
    println("Affichage des ages qui ont une une variable Duration maximale")
    val Durmax=sqlContext.sql("SELECT Age,Duration from  TableSQLClients where Duration = (select max(Duration) from TableSQLClients)")
    Durmax.show()
     
     //Find ages with minimum duration   
    println("Affichage des ages qui ont une une variable Duration maximale")
    val Durmin=sqlContext.sql("SELECT Age,Duration from  TableSQLClients where Duration = (select min(Duration) from TableSQLClients)")
    Durmin.show()
    
    //Plus de requetes  
    println("Affichage de plus de requetes")
    sqlContext.sql("SELECT Creditability, avg(Balance) as avgbalance, avg(Amount) as avgamt, avg(Duration) as avgdur  FROM TableSQLClients GROUP BY Creditability ").show

    dfClients.describe("Balance").show
    dfClients.groupBy("Creditability").avg("Balance").show
   
    //define the feature columns to put in the feature vector
    
   
  def parseData(inpLine: String): Array[Double] = {
    val values = inpLine.split(',')
    val Balance = values(0).toDouble
    val Duration = values(1).toDouble
    val History = values(2).toDouble
    val Purpose = values(3).toDouble
    val Amount = values(4).toDouble
    val Savings = values(5).toDouble 
    val Employment = values(6).toDouble
    val InstPercent = values(7).toDouble
    val SexMarried = values(8).toDouble
    val Guarantors = values(9).toDouble
    val ResidenceDuration = values(10).toDouble
    val Assets = values(11).toDouble
    val Age = values(12).toDouble
    val ConcCredit = values(13).toDouble
    val Apartment = values(14).toDouble
    val Credits = values(15).toDouble
    val Occupation = values(16).toDouble
    val Dependents = values(17).toDouble
    val HasPhone = values(18).toDouble
    val Foreign = values(19).toDouble
    
              
    return Array(Balance, Duration, History, Purpose, Amount,
    Savings, Employment, InstPercent, SexMarried,  Guarantors,
    ResidenceDuration, Assets,  Age, ConcCredit, Apartment,
    Credits,Occupation,Dependents,HasPhone,Foreign)
    
    
  }

     

  val dataFile = sc.textFile("/home/cloudera/FileCredit.csv")
  val FileRDD = dataFile.map(line => parseData(line))

  val vectors: RDD[Vector] = FileRDD.map(v => Vectors.dense(v))
  
  val normalizer = new Normalizer()
    
  val l1NormData = normalizer.transform(vectors)
  
  println("Matrice de correlation")
  
  val corr = Statistics.corr(l1NormData)
  println(corr)
  
   
     // Machine learning using the decision tree algorithm
     // Create a labeled point RDD
    
    def FileDataToLP(inpArray: Array[Double]): LabeledPoint = {
    return new LabeledPoint(inpArray(0), Vectors.dense(inpArray(1), inpArray(2), inpArray(3),
      inpArray(4), inpArray(5), inpArray(6), inpArray(7), inpArray(8), inpArray(9),
      inpArray(10), inpArray(11), inpArray(12), inpArray(13), inpArray(14), inpArray(15), inpArray(16),
      inpArray(17), inpArray(18), inpArray(19)))
  }
    val FileRDDLP = FileRDD.map(x => FileDataToLP(x)) 
    println(FileRDDLP.count())
    println(FileRDDLP.first().label)
    println(FileRDDLP.first().features)

   //  Split the dataframe into training and test data
    
val splitSeed = 5043
val Array(trainingData, testData) = FileRDDLP.randomSplit(Array(0.8, 0.2), splitSeed)

  //  DecisionTree model

val numClasses = 2
val categoricalFeaturesInfo = Map[Int, Int]()
val impurity = "gini"
val maxDepth = 5
val maxBins = 32

val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
  impurity, maxDepth, maxBins)
  
  // Evaluate model on test instances and compute test error
val labelAndPreds = testData.map { point =>
  val prediction = model.predict(point.features)
  (point.label, prediction)
  
  }
    
val testErr = labelAndPreds.filter(r => r._1 != r._2).count().toDouble / testData.count()
println(s"Test Error = $testErr")
println(s"Learned classification tree model:\n ${model.toDebugString}")

println("Fin du programme")
    
  }
}