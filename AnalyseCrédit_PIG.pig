--Analyse Cr�dit:
--Combien parmi les hommes ou femmes sont ceux qui ont été admissibles à obtenir un crédit ? 

REGISTER /usr/lib/pig/piggybank.jar;

credits = LOAD '/home/cloudera/ProjetBD6/BD6_germandata.txt' USING PigStorage(' ') AS (statutsoldecomptecourant:chararray, historiquecompte:int, historiquecredit:chararray, objectifcredit:chararray, montantcredit:int, soldecomptedepargne:chararray, dureeemploiactuel:chararray, tauxversement:int, statutpersonneletgenre:chararray, debiteursgarants:chararray, dureedresidenceactuelle:int, proprieteouautresbiens:chararray, age:int, autresplansversement:chararray, logement:chararray, nombrecreditsexistants:int, emploi:chararray, nombrepersonnesgarants:int, telephone:chararray, etranger:chararray, cotecredit:chararray);

equivalance = LOAD '/home/cloudera/ProjetBD6/BD6_equivalance.csv' USING org.apache.pig.piggybank.storage.CSVExcelStorage (';','NO_MULTILINE','UNIX','SKIP_INPUT_HEADER');

equi = foreach equivalance generate  (chararray)$0 as reference,(chararray)$2 as description;

H1 = filter credits by objectifcredit is not null;

H2 = filter H1 by cotecredit=='1';

H3 = group H2 by statutpersonneletgenre;

H4 = foreach H3 generate group, COUNT(H2.cotecredit) as totalcreditobtenus;

H5 = join H4 by group, equi by reference;

H6 = foreach H5 generate description, totalcreditobtenus;

H7 = order H6 by totalcreditobtenus DESC;

STORE H7 INTO '/home/cloudera/ProjetBD6/outputQ3.1';


H2 = filter H1 by cotecredit=='2';

H3 = group H2 by statutpersonneletgenre;

H4 = foreach H3 generate group, COUNT(H2.cotecredit) as totalcreditobtenus;

H5 = join H4 by group, equi by reference;

H6 = foreach H5 generate description, totalcreditobtenus;

H7 = order H6 by totalcreditobtenus DESC;

STORE H7 INTO '/home/cloudera/ProjetBD6/outputQ3.2';
