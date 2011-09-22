function(doc) {
	if(!doc.imports || !doc.primaryType)
		return;

	var type2fingerprint = new Object();
	for(var i=0; i<doc.imports.length; i++) {
		var _import = doc.imports[i];
		type2fingerprint[_import.name] = _import.fingerprint;
	}

	for(var i=0; i<doc.primaryType.methods.length; i++) {
		var method = doc.primaryType.methods[i];
		for(var j=0; j<method.objects.length; j++) {
			var object = method.objects[j];
			var fingerprint = type2fingerprint[object.type];
			if(fingerprint != null && fingerprint != "") {
				var objectUsage = {
					"type" : object.type,
					"contextFirst" : method.firstDeclaration,
					"contextSuper" : method.superDeclaration,
					"calls" : []
				}
				for(var k=0; k<object.receiverCallSites.length; k++) {
					objectUsage.calls.push(object.receiverCallSites[k].targetMethod);
				}
				if(objectUsage.calls.length > 0) {
					emit(fingerprint, objectUsage);
				}
			}
		}
	}
}



function (key, values, rereduce) {
	if(rereduce)
		return sum(values);
	else
		return values.length;
}