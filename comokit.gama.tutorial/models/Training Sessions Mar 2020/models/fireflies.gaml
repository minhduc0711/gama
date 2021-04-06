/**
* Name: fireflies
* Based on the internal empty template. 
* Author: minhduc0711
* Tags: 
*/


model fireflies

/* Insert your model definition here */

global {
	float diffusionSpeed <- 0.5;
	int num_off_fireflies <- 0 update: firefly_cell count !each.is_on;
	float max_diff <- 0.0 update: max(firefly_cell accumulate each.duration_on) -
			min(firefly_cell accumulate each.duration_on);
}

grid firefly_cell width: 50 height: 50 {
	bool is_on;
	float duration_on;
	float switch_on_every;
	float time_still_on;
	
	init {
		duration_on <- rnd(10.0);
		switch_on_every <- rnd(20.0) + 1;
		is_on <- flip(0.5);
	}
	
	reflex update {
		if (time_still_on > 0) {
			time_still_on <- time_still_on - 1;
		} else {
			do switch_off;
		}
	}
	
	reflex switch_on when: mod(cycle, switch_on_every) = 0 {
		color <- #lightgreen;
		is_on <- true;
		time_still_on <- duration_on;
	}
	
	reflex diffuse {
		firefly_cell neighbor <- one_of(neighbors);
		
		float dist1 <- neighbor.duration_on - duration_on;
		duration_on <- duration_on + diffusionSpeed * dist1;
		neighbor.duration_on <- (neighbor.duration_on - diffusionSpeed * dist1);
		
		float dist2 <- neighbor.switch_on_every - switch_on_every;
		switch_on_every <- switch_on_every + diffusionSpeed * dist2;
		neighbor.switch_on_every <- (neighbor.switch_on_every - diffusionSpeed * dist2);
	}
	
	action switch_off {
		color <- #darkgreen;
		is_on <- false;
	}
}

experiment exp {
	output {
		display my_display type: java2D {
			species firefly_cell;
		}
		display graphs {
			chart "graph" type: series {
				data "number of off fireflies" value: num_off_fireflies;
			}
			chart "graph2" type: series {
				data "max duration_on diff" value: max_diff;
			}
		}
	}
}
