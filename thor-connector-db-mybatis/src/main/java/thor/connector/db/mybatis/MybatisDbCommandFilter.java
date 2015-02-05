package thor.connector.db.mybatis;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import thor.connector.command.CommandExecutionCallback;
import thor.connector.command.db.DBCommand;

/**
 * Created by chanwook on 2015. 2. 4..
 */

//MappedStatement.class,Object.class,RowBounds.class,ResultHandler.class
@Intercepts({
	@Signature(type=Executor.class,method="query",args={MappedStatement.class,Object.class,RowBounds.class,ResultHandler.class})
})
public class MybatisDbCommandFilter implements Interceptor {
	
	private final int mappedStatementIndex = 0;
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
	
		String statementId = extractMappedStatementId(invocation);
		return new DBCommand<Object>(statementId, new MybatisCommandExecutionCallback(invocation)).execute();
	}

	private String extractMappedStatementId(Invocation invocation) {
		
		MappedStatement ms = (MappedStatement) invocation.getArgs()[mappedStatementIndex];
		return ms.getId();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {

	}
	
	class MybatisCommandExecutionCallback implements CommandExecutionCallback<Object> {
		
		private Invocation invocation;
		
		public MybatisCommandExecutionCallback(Invocation invocation) {
			this.invocation = invocation;
		}

		@Override
		public Object execute() {
			try {
				return invocation.proceed();
			} catch (InvocationTargetException | IllegalAccessException e) {
				throw new MybatisExecuteFailException(e);
			}
		}
		
	}
}