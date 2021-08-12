+ Quarks {
	*updateAll {arg exclude=["jtSC"];
		exclude=exclude??{[]};
		Quarks.installed.do{|q|
			if (exclude.includesEqual(q.name.asString).not, {
				Quarks.update(q.name.asString)
			})
		}
	}
}

//Quarks.update("atk-sc3")
//["jtSC"].includesEqual("jtSC")