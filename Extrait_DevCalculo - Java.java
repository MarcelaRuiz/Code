
import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;



/**
* Esta clase es la que representa el tablero 
* y se encarga de la logica para resolver el calculo
* */
public class Tablero implements Cloneable {


    /**
     * Costructor del tablero inicializa cada cuadro del tablero si es un cuadro fijo lo especifica y si esta
     * en blanco este se crea con las posibilidades de 1-3 que puede tener
     * */
    Tablero() {
        tablero = new Cuadro[DIMENSION] [DIMENSION];
         for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j < tablero[i].length; j++){
                if (tablero[i][j] == null) {
                	tablero[i][j] = new Cuadro(i,j);
                }
    			vertices.add(tablero[i][j]);
                back.add(tablero[i][j]);
            }
        }
    }

public void leerTablero(int[][] matriz){
    int vacios=0;
      for(int i = 0; i <3 ; i++){
            for(int j = 0; j <3 ; j++){
                if(matriz[i][j]!=0){
					tablero[i][j] = new Cuadro(0,0,matriz[i][j]);
                }else{
                    tablero[i][j] = new Cuadro(i,j);
                   	vacios++;
                }
	            vertices.add(tablero[i][j]);
                back.add(tablero[i][j]);
            }
        }
      validarSoluciones(vacios);
}

 public void validarSoluciones(int vacios){
        int uno = 1;
        int facto = 1;
     	int num = 0;
     	while (num!=vacios){
     		num=num+1;
     		facto =facto*num;
     	}
     	numSol= facto;
 }

    /**
     * @param Cuadro
     * Retorna la lista con los cuadros tanto verticales, horizontales 
     * los cuales son los requerimientos del calculo ninguno puede tener el mismo numero de 1-9
     * @return List
     * */
	public List getVecinos(Cuadro c){
		int x = c.getX();
		int y = c.getY();
		List lresp = new ArrayList();
		for (int i = 0; i < DIMENSION; i ++){
			if (i != x) {
				lresp.add(tablero[i][y]);
			}
			if (i!=y) {
				lresp.add(tablero[x][i]);
			}
		}
        int inix = (x / 3)*3;
		int finx = inix + 3;
		int iniy = (y / 3) *3;
		int finy = iniy + 3;
		for (int i = inix; i < finx; i++) {
			for (int j = iniy; j < finy; j++){
				if (i != x && j != y)
					lresp.add(tablero[i][j]);
			}
		}
		return lresp;
	}

    public static Tablero getTablero (){
		return t;

    }

   public Cuadro[][] getTableroM (){
		return this.tablero;

    }

    /**
     * @param Tablero
     * Este metodo tienen la logica principal para encontrar el resultado de cada cuadro
     * @return boolean
     * */
     boolean solucion (Tablero t) {
        // si vertices es cero es porque ya tosod los cuadros tienen numeros fijos
     	if (t.vertices.size() == 0) {
             this.t = t;
               
               if(i<numSol){
               i++;
               System.out.println("sali true " +i);
               mostrar(this.t);
               }else{
                 return true;
               }

     		return false;

        }
     	else {
            // se recorre el los vertices del primero al final
     		Cuadro cuadro = (Cuadro) t.vertices.first();
        	if (cuadro.getNumeros().size() == 1) {
             // Si los cuadros ya son fijos o ya estan definidos se borran de sus vecinos este numero y
             // asi se reduce el conjunto de todos los cuadros
        		t.vertices.remove(cuadro);
        		List lisVecinos = t.getVecinos(cuadro);
        		Iterator i = lisVecinos.iterator();
        		while (i.hasNext()){
        			
        			Cuadro vecino = (Cuadro) i.next();
                    boolean b = t.vertices.remove(vecino);
	        	    List candidatos = vecino.getNumeros();
                    candidatos.remove(cuadro.getNumeros().get(0));
	        	    
	        	    if (candidatos.size()==1 && !vecino.getFijo()){
	        	    	vecino.setFijo(true);
                    }
	        	    else if (candidatos.size() == 0) {
                            return false;
                         }
	        	         
	        	   if (b){
                         t.vertices.add(vecino);
                   }
                   
                 }

                 return solucion(t);
            }
           else if (cuadro.getNumeros().size() == 0 ){
              // si no hay numeros para escojer en algun cuadro
              // el ku no tiene solucion
               		return false;
                }else {
               // si el cuadro tiene mas de una posibilidad de numero prueba con cada uno enpezando por el primer
               // si no se encuentra solucion se intanta con el siguiente numero hasta agotar las posibilidades
                cuadro = (Cuadro) t.vertices.first();
           		List numeros = new ArrayList(cuadro.getNumeros());
           		Iterator it = numeros.iterator();
           		
           		while (it.hasNext()){

           			Integer n = (Integer) it.next();
           			List lis = new ArrayList();
           			lis.add(n);
                    cuadro.setNumeros(lis);
           			cuadro.setFijo(true);
                    Tablero  t1 =  t.copia();
           			if (solucion(t1)){
                        return true;
           			}
           		}
           System.out.println("sali 4");
   	       		return false;   // No hay solucion
          }

     	}

      }

	  public void mostrar(Tablero t){
		
			   int[][] solucion= new int[DIMENSION] [DIMENSION];
			   Cuadro[][] tab=t.getTableroM();

					 for (int i = 1; i <= DIMENSION ; i++){
							for (int j = 1; j <=DIMENSION; j++){
									int u=i*j;
									Cuadro c=tab[i-1][j-1];
			
										Integer h=(Integer)c.getNumeros().get(0);
										String s=String.valueOf(h.intValue());
										solucion[i-1][j-1]=h.intValue();
										System.out.print(" " + s + ",");
							}
							System.out.println("/" );
					 }
			   soluciones.add(solucion);
			}
		
	  public int[][] mandarsoluciones(int num){

				int[][] matrix= new int[DIMENSION] [DIMENSION];
				matrix=(int[][])soluciones.get(num);
                for(int i=0;i<3;i++){
					for(int j=0;j<3;j++){
					//	System.out.println(matrix[i][j]);
                    }
				//	System.out.println("");
                }
                System.out.println("Tablero-mandarSoluciones "+soluciones.size());
				return matrix;
	}
		
	  public int[][] mandarsoluciones(){
				int[][] matrix= new int[DIMENSION] [DIMENSION];
				Iterator i = soluciones.iterator();
				while (i.hasNext()){
						 matrix=(int[][])i.next();
						 soluciones.remove(matrix);
						 return matrix;
				}
				return matrix;
			 }

      public int  enviarNumSol(){
		 	return numSol;
      }

	static public void main(String [] args) {

        // TreeSet vert = t.vertices;
      long temp1 = System.currentTimeMillis();
	//	if(t.solucion_2(t,0)){
       if (t.solucion(t)){
        	System.out.println("SI");
        }else {
        	System.out.println("NO");
        }
        long temp2 = System.currentTimeMillis();

		System.out.println(temp2 - temp1);

        //System.out.println(t.vertices);
        System.out.println("----------------------------");
        for (int i = 0; i < t.DIMENSION ; i++)
        	for (int j = 0; j < t.DIMENSION; j++)
        	    System.out.println(t.tablero[i][j]);



    }

/**
* Este metodo realiza una copia completa del tablero para siempre mantener una
* imagen sin modificaciones especialmente para la ultima recussion del metodo solucion
* @return Tablero
* */
    public Tablero copia() {
      Tablero copia = new Tablero();
      copia.tablero = new Cuadro [DIMENSION][DIMENSION];

      for (int i = 0; i < this.tablero.length; i++) {
        for (int j = 0; j < this.tablero[i].length; j++) {
            copia.tablero[i][j] = this.tablero[i][j].copia();
        }
      }

      Iterator it = this.vertices.iterator();
      copia.vertices = new TreeSet();

      while (it.hasNext()) {
        Cuadro c = (Cuadro) it.next();
        copia.vertices.add(copia.tablero[c.getX()][c.getY()]);
      }

      Iterator it2 = this.back.iterator();
      copia.back = new ArrayList();

      while (it2.hasNext()) {
        Cuadro c = (Cuadro) it2.next();
        copia.back.add(copia.tablero[c.getX()][c.getY()]);
      }
      return copia;
    }

    public void AsignarCuadro(int x, int y, int valor){
		 tablero[x][y] = new Cuadro(x,y,valor);

    }

	private int numSol=0;
    private ArrayList soluciones= new ArrayList();
	private int i=0;
    static private Tablero t = new Tablero();
    private final int DIMENSION = 3;
    private Cuadro[][] tablero;
    private List back= new ArrayList();
    private TreeSet vertices = new TreeSet();// Conjunto que mantiene los numeros posibles de cada cuadro en orden ascendente
                                             //desde los cuadros que estan fijos en tal caso tienen un solo numero
}
