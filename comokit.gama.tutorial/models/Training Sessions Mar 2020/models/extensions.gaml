/**
* Name: extensions
* Based on the internal empty template. 
* Author: minhduc0711
* Tags: 
*/


model extensions

/* Insert your model definition here */

global {
	tutorial myTut;
	
	init {
		tutorial a <- tutorial(["Patrick"]::1);
		tutorial b <- tutorial(["Alexis"]::2);
		tutorial c <- tutorial(["Arthur"]::19);
		
		tutorial agg <- aggregate_tutorials([a,b,c]);
//		write agg;
		
			
		create teacher number: 5 {
			pedalogical_level <- rnd(5);
			my_tut <- create_tutorial(1);
			write my_tut;
		}
	}
	
//	reflex hi {
//		string s <- say_hello("Duc");
//		write s;
//		say_hi name: "Duc";
//		write myTut;
//	}
}

species teacher skills: [teaching] {
	tutorial my_tut;
}

experiment exp {
	output {
	}
}