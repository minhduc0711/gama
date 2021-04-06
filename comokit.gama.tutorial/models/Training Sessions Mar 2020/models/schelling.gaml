/**
* Name: schelling
* Based on the internal empty template. 
* Author: minhduc0711
* Tags: 
*/


model schelling

global {
	init {
		create people number: 2000;
	}
}

species people {
	rgb color <- flip(0.5) ? #red : #yellow;
	
	float neighbor_dist <- 10.0;
	float similar_rate_wanted <- 0.5;

	reflex check {
		list<people> neighbors <- people at_distance neighbor_dist;
			 
		float rate;
		if empty(neighbors) {
			rate <- 1.0;
		} else {
			rate <- (neighbors count (each.color = self.color)) / length(neighbors);	
		}
		
		if (rate < similar_rate_wanted) {
			location <- any_location_in(world);
		}
	}
	
	aspect base {
		draw circle(2.0) color: color;
	}
}

experiment exp {
	output {
		display my_display type: opengl {
			species people aspect: base;
		}
	}
}