/**
* Name: opiniondiffusion
* Based on the internal empty template. 
* Author: minhduc0711
* Tags: 
*/


model opiniondiffusion

/* Insert your model definition here */

global {
	float speed <- 0.1;
	float thresh <- 0.2;
	
	init {
		create people number: 20 {
			opinion <- rnd(1000) / 1000;
		}
	}
}

species people {
	float opinion;
	
	reflex update {
		ask one_of(people) {
			float diff <- self.opinion - myself.opinion;
			if abs(diff) < thresh {
				myself.opinion <- myself.opinion + speed * diff;
				self.opinion <- self.opinion + speed * -diff;
			}
		}
	}
}

experiment exp {
	output {
		display graphs {
			chart "graph" type: series {
				loop ag over: people {
					data ag.name value: ag.opinion;
				}
			}
		} 
	}
}