CREATE DATABASE GledanjeFilmova;
GO

USE GledanjeFilmova;
go

CREATE TABLE Korisnik(
	 IdKor INT PRIMARY KEY IDENTITY,
	 KorIme NVARCHAR(100) NOT NULL,
	 Nagrade INT DEFAULT 0,
);
CREATE TABLE Film(
	IdFilm INT PRIMARY KEY IDENTITY,
	Naslov NVARCHAR(100) NOT NULL,
    Reziser NVARCHAR(100) NOT NULL,
    Trend NVARCHAR(100) NULL
);

CREATE TABLE Zanr(
	IdZanr INT PRIMARY KEY IDENTITY,
	Naziv NVARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE ZanrFilma(
    IdFilm INT NOT NULL,
    IdZanr INT NOT NULL,
    PRIMARY KEY (IdFilm, IdZanr),
    FOREIGN KEY (IdFilm) REFERENCES Film(IdFilm) ON DELETE NO ACTION ON UPDATE CASCADE,
    FOREIGN KEY (IdZanr) REFERENCES Zanr(IdZanr) ON DELETE NO ACTION ON UPDATE CASCADE
);
CREATE TABLE Tag(
	IdTag INT PRIMARY KEY IDENTITY,
	Naziv NVARCHAR(100) NOT NULL,
);
CREATE TABLE TagFilm(
	IdFilm INT NOT NULL,
    IdTag INT NOT NULL,
    PRIMARY KEY (IdFilm, IdTag),
    FOREIGN KEY (IdFilm) REFERENCES Film(IdFilm) ON DELETE NO ACTION ON UPDATE CASCADE,
    FOREIGN KEY (IdTag) REFERENCES Tag(IdTag) ON DELETE NO ACTION ON UPDATE CASCADE
);
CREATE TABLE ListaZelja(
	IdFilm INT NOT NULL,
    IdKor INT NOT NULL,
    PRIMARY KEY (IdFilm, IdKor),
    FOREIGN KEY (IdFilm) REFERENCES Film(IdFilm) ON DELETE NO ACTION ON UPDATE CASCADE,
    FOREIGN KEY (IdKor) REFERENCES Korisnik(IdKor) ON DELETE NO ACTION ON UPDATE CASCADE
);
CREATE TABLE Ocena(
    IdOcena INT PRIMARY KEY IDENTITY,
    IdFilm INT NOT NULL,
    IdKor INT NOT NULL,
    Ocena DECIMAL(10,3) NOT NULL CHECK (Ocena BETWEEN 1 AND 10),
    Datum DATETIME NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (IdFilm) REFERENCES Film(IdFilm) ON DELETE NO ACTION ON UPDATE CASCADE,
    FOREIGN KEY (IdKor) REFERENCES Korisnik(IdKor) ON DELETE NO ACTION ON UPDATE CASCADE
);
go


create trigger TR_BLOCK_EXTREME
on Ocena
after insert,update
as
begin
	if exists(
		select *
		from inserted i
		where (i.Ocena = 1 or i.Ocena = 10) and exists
		(
			select 1 
			from ZanrFilma zf
			where zf.IdFilm = i.IdFilm and 
			(
				select count(*)
                from Ocena o join ZanrFilma zf2 on zf2.IdFilm = o.IdFilm
                where o.IdKor = i.IdKor and zf2.IdZanr = zf.IdZanr and (o.Ocena = 1 or o.Ocena = 10)
			) > 4
			and
			(
				select count(*)
				from Ocena o join ZanrFilma zf2 on zf2.IdFilm = o.IdFilm
				where o.IdKor = i.IdKor and zf2.IdZanr = zf.IdZanr and (o.Ocena = 6 or o.Ocena = 7 or o.Ocena = 8)
			) < 3
		)
	)
	begin 
		print 'ne sme ekstremna ocena'
		rollback transaction;
	end

end 
go

create trigger  TR_UPDATE_MOVIE_TREND
on Ocena
after insert,update,delete
as
begin
    declare @IdFilm int

    declare filmCursor cursor for
    select IdFilm from inserted union select IdFilm from deleted;
    open filmCursor;

    fetch next from filmCursor into @IdFilm;

    while @@FETCH_STATUS = 0
    begin
   	    if
            (select ISNULL(avg(avgPetOcena.Ocena),0) from (select top 5 Ocena from Ocena where IdFilm = @IdFilm order by Datum desc)avgPetOcena)
            -
            (select ISNULL(avg(Ocena),0) from Ocena where idFilm = @IdFilm)
            >= 1
            begin
                update Film
                set Trend = 'Rising'
                where IdFilm = @IdFilm;
            end


              else if
            (select ISNULL(avg(avgPetOcena.Ocena),0) from (select top 5 Ocena from Ocena where IdFilm = @IdFilm order by Datum desc)avgPetOcena)
            -
            (select ISNULL(avg(Ocena),0) from Ocena where IdFilm = @IdFilm)
            <= -1
            begin
                update Film
                set Trend = 'Falling'
                where IdFilm = @IdFilm;
            end
	
            else if
            (select count(*) from Ocena where IdFilm = @IdFilm) >=3
            and
            (select ISNULL(avg(Ocena),0) from Ocena where IdFilm = @IdFilm) >=8
            begin
                update Film
                set Trend = 'Classic'
                where IdFilm = @IdFilm;
            end

 	    else if 
            (select count(*) from Ocena o where o.IdFilm = @IdFilm and o.Datum >= DATEADD(DAY,-30,GETDATE()))
            =
            (select ISNULL(MAX(brOcena),0) 
            from (select count(*) as brOcena from Ocena where Datum  >= DATEADD(DAY,-30,GETDATE()) group by IdFIlm)brOcenaFilmova
            )
            begin
                update Film
                set Trend = 'Trending'
                where IdFilm = @IdFilm;
            end

           
 
            else
            begin
                update Film
                set Trend = NULL
                where IdFilm = @IdFilm;
            end

            fetch next from filmCursor into @IdFilm;
    end
    close filmCursor;
    deallocate filmCursor;
end 
go


create procedure SP_REWARD_USER_
    @IdKor int,
    @IdFilm int
as
begin
   
   if(select count(*) from Ocena  where IdKor = @IdKor)< 10 return;

   if(select avg(Ocena) from Ocena where IdFilm = @IdFilm and IdKor != @IdKor) >= 6 return;

   if not exists(
        select 1 
        from ZanrFilma z
        where z.IdFilm = @IdFilm and z.IdZanr in (
               select zf.IdZanr
               from ZanrFilma zf join Ocena o on o.IdFilm = zf.IdFilm
               where o.IdKor = @IdKor
               group by zf.IdZanr
               having AVG(o.Ocena) >= 8
               )
   )
   return;

    update Korisnik
    set Nagrade = Nagrade + 1
    where IdKor = @IdKor;
 
end
go

create procedure SP_RECOMMENDED_IDFILM
    @IdKor int
as
begin
    select distinct f.idfilm,(select avg(o.ocena) from ocena o where o.idfilm = f.idfilm) as avrg
    from film f
    join zanrfilma zf on zf.idfilm = f.idfilm
    where zf.idzanr in (
        select zf2.idzanr
        from ocena o2 join film f2 on f2.idfilm = o2.idfilm join zanrfilma zf2 on zf2.idfilm = f2.idfilm
        where o2.idkor = @idkor
        group by zf2.idzanr
        having avg(o2.ocena) >= 8
    )
    and f.idfilm not in (
        select idfilm from ocena where idkor = @idkor
    )
    and f.idfilm not in (
        select idfilm from listazelja where idkor = @idkor
    )
    and (
        ((
            select count(*)
            from ocena o
            where o.idfilm = f.idfilm
        ) >= 4
        and
        (
            select avg(o.ocena)
            from ocena o
            where o.idfilm = f.idfilm
        ) >= 7.5)
        or
        ((
            select count(*)
            from ocena o
            where o.idfilm = f.idfilm
        ) < 4
        and
        (
            select avg(o.ocena)
            from ocena o
            where o.idfilm = f.idfilm
        ) >= 9)
    )
    order by avrg desc, f.idfilm asc;
end
go