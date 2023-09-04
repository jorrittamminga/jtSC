Bell {
	classvar <all;

	*at { |key| ^all.at(key).deepCopy}

	*sjoerd { ^all.at(\Sjoerd).deepCopy }
	*goes { ^all.at(\Goes).deepCopy }
	*domutrecht { ^all.at(\DomUtrecht).deepCopy}

	*initClass {
		all = (
			Goes: ( 'basefreq': 173.16738518945, 'decaytimes': [ 30.353543, 25.341456, 27.335585, 0.103563, 3.25399, 2.76708, 6.789212, 1.977486, 4.536641, 5.134201, 1.501974, 1.929953, 2.121086, 9.203383, 3.558093, 2.929124, 1.175579, 1.130326, 1.774934, 5.956167, 2.431212, 1.054149, 1.385363, 1.529443, 1.086948, 5.234073, 2.280974, 0.740472, 0.727473, 1.779363, 0.706656, 0.102645, 3.679632, 0.479638, 1.234013, 1.165829, 0.48665, 0.289683, 1.207059, 1.83654, 2.692511, 1.042014, 0.858239, 1.354064, 1.596783, 1.102559, 2.609375, 0.433375, 1.541737, 0.916545, 0.808581, 1.281994, 2.119438, 1.084022, 1.101338, 0.768833, 0.679899, 0.974803, 0.97538, 0.886318, 0.832402, 0.524898, 1.311389, 0.589542, 1.182866, 0.501673, 0.704424, 0.811021, 1.16184, 0.848101, 0.54095, 0.834814, 0.630479, 0.809464, 1.136158, 0.641824, 0.531477, 0.681826, 0.960637, 0.422571, 0.695995, 0.692294, 0.398182, 0.773344, 0.123184, 0.367451, 0.472989, 0.636874 ], 'amps': [ 0.0457667945401, 0.11472087162101, 0.17635382282095, 0.013830961727184, 0.015255598180034, 0.020876081720047, 1.0, 0.015565059327325, 0.083894638237132, 0.080919908109554, 0.029072620215899, 0.56312728610938, 0.034319520028548, 0.2331050941208, 0.072979859933984, 0.023733718440538, 0.033918056918548, 0.026203831742349, 0.038147359264877, 0.16992483718441, 0.020555468819699, 0.027043001159783, 0.074318070300654, 0.0067607502899452, 0.0071454857703633, 0.10161756178071, 0.020218128289767, 0.0092364394682844, 0.0061669194397359, 0.02932353465965, 0.0036020162369524, 0.019105740922474, 0.069648273708624, 0.0039616602729948, 0.01252620661968, 0.003376193237577, 0.004014631100009, 0.0049011954679276, 0.021405789990187, 0.0072012445356408, 0.02775671335534, 0.008408421803908, 0.0090691631724508, 0.01312003746989, 0.0034514675707022, 0.0056511508609153, 0.016911633508788, 0.0031531581764654, 0.0049234989740388, 0.0067468105986257, 0.0013075430457669, 0.0093479569988399, 0.016758296904273, 0.0036772905700775, 0.0023335043268802, 0.0017145820322954, 0.0055898162191097, 0.0053389017753593, 0.0017424614149344, 0.001862342760282, 0.0042293023463286, 0.00098414220715493, 0.0051688375412612, 0.0025732670175751, 0.0011346908734053, 0.0010426889106967, 0.0011932375769471, 0.0032200686947988, 0.0033287982870908, 0.001460879650281, 0.0019264653403515, 0.0036745026318139, 0.0015974886252119, 0.0019236774020876, 0.0037107458292442, 0.0017619769827818, 0.0011737220090998, 0.0018428271924347, 0.002174591845838, 0.00075274333125171, 0.0015389419216701, 0.0014302123293781, 0.0014413640824338, 0.00095068694798824, 0.00078619859041841, 0.0010231733428495, 0.0012824516013917, 0.0010482647872245 ], 'ratios': [ 1.0, 2.0074911460726, 2.4276480790795, 2.5666824802374, 2.9713770852095, 3.8737563280385, 4.0078232253748, 4.2629793384152, 4.7776296600584, 4.8858225260266, 4.9899164209828, 5.4225628326183, 5.8446416509766, 5.9893540839267, 6.3519285125442, 7.4044509312319, 7.6939112026344, 7.8641456131793, 7.9901082789165, 8.2725506628249, 8.735040530858, 9.2273154105056, 9.4882400626667, 9.9853893542737, 10.261412196717, 10.802990519486, 10.907967928597, 11.230560423896, 11.566878334186, 12.428204320983, 13.27513340233, 13.491009741495, 13.523541819059, 13.751892751021, 14.135686299853, 14.272881287369, 14.681824144994, 14.986318670579, 15.237604597648, 15.703342611703, 16.362418717695, 16.509294404275, 16.619927447671, 17.821517146804, 18.988047463317, 19.178710076288, 19.293352706268, 19.53633805042, 19.829701221737, 20.019516678371, 20.239436415779, 20.565213500051, 22.287297265542, 22.612667941124, 23.439325668571, 23.762226392137, 24.720473006219, 25.286915496433, 25.547749086754, 25.869113350824, 26.587650205812, 27.277850985207, 28.305192014816, 28.593105637142, 29.022408657391, 29.403767440256, 29.699077296649, 31.005460943619, 31.309829749979, 32.219658274766, 32.457072573266, 33.173792456284, 33.39120332926, 33.830187204245, 34.302601911737, 35.333338507292, 35.505505272294, 35.660402198394, 36.267576694765, 36.461421636326, 37.265502800504, 37.720729232897, 39.717244301256, 40.329914266821, 42.556706942876, 43.106114961331, 43.824767617497, 46.259940644096 ] ),

				Sjoerd: ( 'basefreq': 212.70526970436, 'decaytimes': [ 33.904621, 15.20271, 9.425017, 4.566712, 4.020149, 3.794852, 6.957747, 3.848726, 5.047301, 2.828916, 1.539445, 3.092835, 2.739732, 1.182712, 1.201614, 3.030584, 2.177193, 2.157326, 2.179376, 1.313677, 0.516746 ], 'amps': [ 0.23081623200798, 0.069363688185834, 0.26430810887844, 0.060168163032635, 1, 0.20037231010403, 0.33909077953541, 0.046943138093202, 0.17352322930027, 0.027091349579592, 0.025514821148639, 0.027762932877298, 0.010909220464586, 0.0090690466011116, 0.0048186546957389, 0.013000570044178, 0.0046369531138663, 0.022930027077098, 0.008648639019524, 0.0023995297135528, 0.0018187972067835 ], 'ratios': [ 1, 2.00228014864, 2.386988388056, 3.0166135535133, 3.9865655747207, 5.3151820390429, 6.0405375177243, 6.6041943646729, 8.3269307792301, 8.7470840088872, 9.5825244583099, 10.840741547279, 11.500492290313, 12.164680538149, 12.873237619316, 13.529848453082, 14.706987248554, 16.287023609299, 19.127570854537, 21.989371969751, 25.717346066317 ] ),

			DomUtrecht: ( 'ratio': [ 1.0, 2.0017516695394, 2.3934530457035, 2.9702396425631, 3.4227199151032, 4.0021040043971, 4.4219028309171, 4.7620083717559, 5.4099406810234, 6.0311769365516, 6.6951227203777, 7.6684334115716, 8.2993722785767, 9.0927034371354, 9.8462868617887, 10.843535867309, 12.075059882486, 13.55895002545, 14.749653034813, 16.412407949778, 19.316336620372, 22.303054182644, 24.887613915965, 27.034748438307, 28.88312261509, 32.941961711325, 37.262673319683, 41.061173312359, 44.050483190818, 50.263331551498, 54.525304058032, 64.361706450925, 78.56412241614, 85.110002126014, 95.289590489727, 108.84441322181, 116.59261733893, 125.65783547008 ], 'db': [ -11.592656252317, -9.1320753970349, -7.113420638245, -26.292516338764, -29.016348908173, 0.0, -23.841623696909, -31.174339698331, -17.934324561915, -16.71853766958, -21.561516993709, -22.437887859309, -9.4872604269816, -26.251873510195, -22.741799652545, -9.5417455042907, -18.332647691645, -16.936170728864, -32.293104209619, -23.647882052132, -24.429168303479, -34.050630396149, -36.931850578434, -40.292644088512, -42.655257945928, -42.289618113252, -40.985655057856, -44.887241623955, -53.412298892854, -46.283171647558, -53.627155174473, -51.167313226621, -52.424690770825, -57.793869356346, -56.979719399866, -62.089742226248, -62.774699104951, -64.017657239928 ], 'dur': [ 68.5, 29, 30, 3.298957, 1.132224, 15.541729, 7.212191, 14.427429, 4.664529, 10.999996, 9.278984, 2.133128, 6.777336, 5.703699, 2.496902, 6.824517, 5.754111, 5.307131, 3.307915, 4.426014, 4.135211, 3.139048, 1.956362, 2.034015, 1.784227, 1.32359, 0.201477, 1.512263, 0.780838, 1.537982, 0.923433, 0.433099, 0.262074, 0.044197, 0.241161, 0.145266, 0.136635, 0.046324 ], 'attackTime': [ 0.665765, 0.168475, 0.738324, 0.030905, 0.086319, 0.086327, 0.035348, 0.0125, 0.038608, 0.139045, 0.088887, 0.043419, 0.039955, 0.0711, 0.116205, 0.113611, 0.213789, 0.044784, 0.121819, 0.061546, 0.11183, 0.17913, 0.091765, 0.188314, 0.166483, 0.028756, 0.068687, 0.189238, 0.037077, 0.116673, 0.030247, 0.017904, 0.017791, 0.019197, 0.03003, 0.01703, 0.013808, 0.016549 ],
  'freq': [ 120.50540566095, 241.22189697033, 288.42403020295, 357.92993303731, 412.45625183333, 482.2751665472, 532.86319443299, 573.84775059931, 651.92709636842, 726.78942335214, 806.79847936898, 924.08767904545, 1000.1192231612, 1095.7199162468, 1186.530792534, 1306.7046884891, 1455.1099895193, 1633.9267731535, 1777.4129223185, 1977.7838778611, 2327.7229803214, 2687.6385917578, 2999.0920108765, 3257.8333275, 3480.5724074865, 3969.6844592909, 4490.3535644, 4948.0933469206, 5308.3213464706, 6057.0031584844, 6570.5938843, 7755.9335449, 9467.4014421538, 10256.215332, 11482.910757231, 13116.340169222, 14050.0406495, 15142.4484378 ], 'startTime': [ 0.034211, 0.031277, 0.011696, 0.012217, 0.012046, 0.037311, 0.011469, 0.057617, 0.01246, 0.011029, 0.010497, 0.029278, 0.036275, 0.128825, 0.011961, 0.036402, 0.011184, 0.030271, 0.053275, 0.010569, 0.037174, 0.070379, 0.007656, 0.011857, 0.033509, 0.045806, 0.010755, 0.009233, 0.036739, 0.033795, 0.012467, 0.031091, 0.009277, 0.024431, 0.0124, 0.008461, 0.006692, 0.008569 ], 'decayTime': [ 68.5, 29, 30, 3.268052, 1.045905, 15.455402, 7.176843, 14.414929, 4.625921, 10.860951, 9.190097, 2.089709, 6.737381, 5.632599, 2.380697, 6.710906, 5.540322, 5.262347, 3.186096, 4.364468, 4.023381, 2.959918, 1.864597, 1.845701, 1.617744, 1.294834, 0.13279, 1.323025, 0.743761, 1.421309, 0.893186, 0.415195, 0.244283, 0.025, 0.211131, 0.128236, 0.122827, 0.029775 ], 'fundamental': 120.50540566095 )

		);
		all[\DomUtrecht][\ratios]=all[\DomUtrecht][\ratio];
		all[\DomUtrecht][\amps]=all[\DomUtrecht][\db].dbamp;
		all[\DomUtrecht][\decaytimes]=all[\DomUtrecht][\dur];
		all[\DomUtrecht][\basefreq]=all[\DomUtrecht][\fundamental];
		all.keys.do{|key| all[key][\decaytimesnormalized]=all[key][\decaytimes]/all[key][\decaytimes].maxItem};
	}
}