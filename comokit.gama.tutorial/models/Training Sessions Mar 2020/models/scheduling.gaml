/**
* Name: scheduling
* Based on the internal empty template. 
* Author: minhduc0711
* Tags: 
*/


model scheduling

/* Insert your model definition here */

global {
	list<int> lvalues;
	int num_people <- 100;
	int max_diff <- 0 update: max(people accumulate each.money) -
			min(people accumulate each.money);
	
	reflex fill {
		loop times: length(people) {
			lvalues << rnd(100) + 1;
		}
	}
	
	init {
		create people number: num_people;
	}
}

//species people schedules: shuffle(people) {
species people schedules: people sort_by each.money {
	int money;
	
	reflex withdraw {
		int val <- max(lvalues);
		lvalues >> val;
		money <- money + val;
	}
}

experiment exp {
	output {
		display graphs {
			chart "graph" type: series {
				data "max diff" value: max_diff;
			} 
		}	
	}
}