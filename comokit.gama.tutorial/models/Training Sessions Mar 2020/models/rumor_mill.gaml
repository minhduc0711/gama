/**
* Name: rumormill
* Based on the internal empty template. 
* Author: minhduc0711
* Tags: 
*/


model rumormill

global torus: true {
	int num_init <- 5;
	int num_ppl_heard <- num_init update: people_cell count each.heard_rumor;
	init {
		ask num_init among people_cell {
			heard_rumor <- true;
		}
	}
}

grid people_cell width: 50 height: 50 {
	bool heard_rumor <- false;
	
	reflex tell when: heard_rumor {
		ask one_of(neighbors) {
			heard_rumor <- true;
		}
	}
	
		
	reflex update_color {
		color <- heard_rumor ? #red : #white;
	}
}

experiment exp {
	output {
		display my_display type: java2D {
			species people_cell;
		}
		display graphs {
			chart "graph" type: series {
				data "number of ppl who've heard" value: num_ppl_heard;
			}
		}
	}
}