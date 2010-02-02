
class PojoBuilder {
	def m = {}
	def alias = []
	
	public static Object build(classMap, Class cls, Closure c){
		PojoBuilder b = new PojoBuilder()
		b.m = {String name, args -> b.missing(delegate,name,args)}
		b.alias = classMap
		b.build(cls,c)
	}
	
	public Object build(Class cls, Closure c){
		cls.metaClass.methodMissing = m
		Object o = cls.newInstance()
		c.delegate = o
		c.resolveStrategy = Closure.DELEGATE_FIRST
		c.call()
		return o
	}
	
	public missing(Object o, String name, args){
		if(args.length != 1 || !Closure.isInstance(args[0]))
			throw new MissingMethodException(name, o.class, args)
		
		Class cls = alias[name]
		if(cls == null)
			throw new MissingMethodException(name, o.class, args)
		
		Class[] classes = [cls]
		def f = o.class.metaClass.pickMethod("add", classes)
		if(f == null){
			Object p = o.metaClass.getProperty(o.class, o, name, false, true)
			if(ArrayList.isInstance(p)){
				o.class.metaClass."$name" = { Closure c ->
					o.metaClass.getProperty(delegate.class, delegate, name, false, true)
					.add(build(cls, c))
				}
				p.add(build(cls, args[0]))
			}
			else
				throw new MissingMethodException(name, o.class, args)
		} else{
			o.class.metaClass."$name" = { Closure c ->
				def Object[] objs = [build(cls,c)]
				f.invoke(delegate, objs)
			}
			def Object[] objs = [build(cls,args[0])]
			f.invoke(o, objs)
		}
	}
}

