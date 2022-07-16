package ject.lucene

import ject.lucene.field.LuceneField
import org.apache.lucene.search.*
import org.apache.lucene.util.QueryBuilder

object BooleanQueryBuilderExtensions {

  implicit class BooleanQueryBuilderExtension(val self: BooleanQuery.Builder) extends AnyVal {

    def addPhraseQuery(builder: QueryBuilder)(
      field: LuceneField,
      queryText: String,
      operator: BooleanClause.Occur,
      boost: Float = 1.0f
    ): BooleanQuery.Builder = {
      // `createPhraseQuery` can return null in certain cases. It seems like stopSets can cause that for
      val queryOpt = Option(builder.createPhraseQuery(field.entryName, queryText))

      queryOpt.foreach { query =>
        if (boost == 1.0) self.add(query, operator) else self.add(new BoostQuery(query, boost), operator)
      }

      self
    }

    def addBooleanQuery(builder: QueryBuilder)(
      field: LuceneField,
      queryText: String,
      operator: BooleanClause.Occur,
      boost: Float = 1.0f
    ): BooleanQuery.Builder = {
      // `createBooleanQuery` can return null in certain cases. It seems like stopSets can cause that for
      val queryOpt = Option(builder.createBooleanQuery(field.entryName, queryText))

      queryOpt.foreach { query =>
        if (boost == 1.0) self.add(query, operator) else self.add(new BoostQuery(query, boost), operator)
      }

      self
    }

    def addTermQuery(
      field: LuceneField,
      queryText: String,
      operator: BooleanClause.Occur,
      boost: Float = 1.0f
    ): BooleanQuery.Builder = {
      val query = new TermQuery(field.term(queryText))
      if (boost == 1.0f) self.add(query, operator) else self.add(new BoostQuery(query, boost), operator)
      self
    }

    def addPrefixQuery(
      field: LuceneField,
      queryText: String,
      operator: BooleanClause.Occur,
      boost: Float = 1.0f
    ): BooleanQuery.Builder = {
      val query = new PrefixQuery(field.term(queryText))
      if (boost == 1.0f) self.add(query, operator) else self.add(new BoostQuery(query, boost), operator)
      self
    }

    def addWildcardQuery(
      field: LuceneField,
      queryText: String,
      operator: BooleanClause.Occur,
      boost: Float = 1.0f
    ): BooleanQuery.Builder = {
      val query = new WildcardQuery(field.term(queryText))
      if (boost == 1.0f) self.add(query, operator) else self.add(new BoostQuery(query, boost), operator)
      self
    }
  }
}
