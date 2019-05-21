import json # pour lire fichier JSON
import pymysql.cursors # pour lire une table MYSQL et recevoir une liste de dictionnaires
import pandas as pd # pour utiliser de dataframes
from sqlalchemy import create_engine # pour ecrire un dataframe dans une BD(MYSQL specifique)


#EXTRACTION :
#Lecture du fichier JSON
def lireJson():
    path = "C:\\Users\\1895919\\Documents\\TP1_BD4\\Sources\\cust_data.json"
    with open(path) as json_file:
        lignes2 = json.load(json_file)
        json_file.close()
        lignesA = standariserDict(lignes2,'json')
        return lignesA

#Lecture MySQL de la table source client_DATA
def lireMySQL():
    conn = pymysql.connect(host='localhost',
                           user='root',
                           passwd='',
                           db='tp1db4',
                           cursorclass=pymysql.cursors.DictCursor)
    requete = '''select * from client_DATA'''
    curseur = conn.cursor()
    curseur.execute(requete)
    lignes = curseur.fetchall()
    lignesB = standariserDict(lignes,'mysql')
    return lignesB

#Lecture du fichier CSV
def lireCSV():
    filename = "C:\\Users\\1895919\\Documents\\TP1_BD4\\Sources\\week_cust.csv"
    obj_file = open(filename)
    listing = obj_file.readlines()
    i = 0
    id1, first_name1, last_name1, email1, gender1, ville1 = listing[0].strip('\n').split(",")
    listeA=[]
    for ligne in listing[1:]:
        dict2 = {}
        id, first_name, last_name, email, gender, ville = ligne.strip('\n').split(",")

        if id == '': first_name = None
        if last_name == '': last_name = None
        if email == '': email = None
        if gender == '': gender = None
        if ville == '': ville = None

        dict2.update({id1: int(id)})
        dict2.update({first_name1:first_name})
        dict2.update({last_name1: last_name})
        dict2.update({email1: email})
        dict2.update({gender1 :gender})
        dict2.update({ville1 : ville})
        listeA.append(dict2)

    obj_file.close()
    lignesC = standariserDict(listeA, 'csv')
    return lignesC

#TRANSFORMATION:
#Standardisation de données
def standariserDict(lignes2, file):
    for ligne in lignes2:
        #Ajout des Null dans les champs vides
        if 'id' not in ligne:
            ligne.update({'id': None})
        if 'gender' not in ligne:
            ligne.update({'gender': None})
        if 'first_name' not in ligne:
            ligne.update({'first_name': None})
        if 'last_name' not in ligne:
            ligne.update({'last_name': None})
        if 'email' not in ligne:
            ligne.update({'email': None})
        if 'ville' not in ligne:
            ligne.update({'ville': None})

        #Changement du label general ID pour identifier l'ID et la source
        if file=='csv':
            ligne['id_csv'] = ligne.pop('id')

        if file=='json':
            ligne['id_json'] = ligne.pop('id')

        if file=='mysql':
            ligne['id_mysql'] = ligne.pop('id')

        #Changement du gender FEMALE/MALE par l'abreviation F/M
        for k, v in ligne.items():
            if isinstance(v, str):
               ligne[k] = v.upper()
            if k=='gender' and v=='Female':
                ligne[k] = 'F'
            else:
                if k == 'gender' and v == 'Male':
                    ligne[k] = 'M'

    return lignes2

#Fusionner les données
def mergerData(dfJson, dfCSV, dfMYSQL):
    merge1 = pd.merge(dfJson, dfCSV, how='outer')
    merge2 = pd.merge(dfMYSQL,merge1, how='outer')
    return merge2

#CHARGEMENT :
#Charger les données à nouvelle table MySQL de destination
def writeBD(dataf):
    engine = create_engine('mysql://root@localhost/tp1db4', echo=False)
    commande = ''' create table if not exists client_dest (
    	id INT not null auto_increment,
    	first_name VARCHAR(50),
    	last_name VARCHAR(50),
    	email VARCHAR(50),
    	gender VARCHAR(50),
    	ville VARCHAR(50), 
    	id_csv INT,
    	id_json INT,
    	id_mysql INT,
    	primary key(id)
    )
    '''
    engine.execute(commande)
    dataf.to_sql(name='client_dest', con=engine, if_exists = 'append', index=False)


def main():
    dfJson = pd.DataFrame(lireJson())
   # print (dfJson)
    dfCSV = pd.DataFrame(lireCSV())
   # print (dfCSV)
    dfMYSQL = pd.DataFrame(lireMySQL())
   # print(dfMYSQL)
    merge=mergerData(dfJson, dfCSV, dfMYSQL)
    writeBD(merge)


if __name__ == '__main__':
    main()