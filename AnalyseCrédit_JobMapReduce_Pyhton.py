#Analyse Crédit : Quel est le nombre maximal de crédits accordés ou refusés selon le type d'emploi?

from mrjob.job import MRJob
from mrjob.step import MRStep

class MaxCreditRangeAgeProfesion(MRJob):

    def configure_options(self):
        super(MaxCreditRangeAgeProfesion, self).configure_options()
        self.add_file_option('--item', help='Path to BD6_equivalence.csv')

    def steps(self):
        return [
            MRStep(mapper=self.mapper_get_CreAgePro,
                   reducer_init=self.reducer_init,
                   reducer=self.reducer_count_CreAgePro),
            MRStep(mapper = self.mapper_passthrough,#This mapper does nothing; it's just here to avoid a bug in some versions of mrjob related to "non-script steps." Normally this wouldn't be needed.
                   reducer = self.reducer_find_max_CreAgePro)
        ]

    def mapper_get_CreAgePro(self, _, line):
        listline = line.split(' ')
        rage=' '
        age=int(listline[12])
        if age>=19 and age<=27:
            rage = 'RangeAge19-27'
        elif age>=28 and age<=33 : 
            rage = 'RangeAge28-33'
        elif age>=34 and age<=36 : 
            rage = 'RangeAge34-36'   
        elif age>=37 and age<=42 : 
            rage = 'RangeAge37-42' 
        elif age>=43 and age<=75 : 
            rage = 'RangeAge43-75' 
           
           
        if (listline[20] == '1') :
            yield ('RangeAgeEtEmploiAvecPlusDeCreditsAcceptes', listline[16], rage), 1
        else : 
            yield ('RangeAgeEtEmploiAvecPlusDeCreditsRefuses', listline[16], rage), 1
            
    def reducer_init(self):
        self.equivalences = {}

        with open("BD6_equivalance.csv") as f:
            for line in f:
                fields = line.split(';')
                self.equivalences[fields[0].strip(' ')] = fields[2]

    def reducer_count_CreAgePro(self, key, values): 
        emploi = unicode(self.equivalences[key[1]], "utf-8", errors="ignore")
          
        yield (key[0],key[2]),(sum(values), emploi)

    def mapper_passthrough(self, key, value):#This mapper does nothing; it's just here to avoid a bug in some versions of mrjob related to "non-script steps." Normally this wouldn't be needed.
        yield key, value


    def reducer_find_max_CreAgePro(self, key, keyvalue):
        yield key, max(keyvalue) #python toma la primera columna#valor y te da el max o min ou outre

if __name__ == '__main__':
    MaxCreditRangeAgeProfession.run()


#!python /home/cloudera/ProjetBD6/MaxCreditRangeAgeProfesion.py --item=/home/cloudera/ProjetBD6/BD6_equivalance.csv /home/cloudera/ProjetBD6/BD6_germandata.txt  > /home/cloudera/ProjetBD6/MaxCreditRangeAgeProfesion.txt